package cz.filmtit.dataimport.alignment.aligners.distance

import scala.collection.mutable.ListBuffer
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.model.SubtitleFile

/**
 * Abstract class for counting the chunks with best distances in a file
 * together with the sum of the distances
 *
 * What is abstract is the way of actually counting a distance of two subtitle files
 */
abstract class FilePairCounter {

  /**
   * Counts distance of two subtitles   (abstract)
   *
   * @param start1 Start of first chunk
   * @param start2 Start of second chunk
   * @param end1 end of first chunk
   * @param end2 end of second chunk
   * @return distance of the chunks
   */
    def timeDistance(start1:Long, start2:Long, end1:Long, end2:Long):Long

  /**
   * Converts time to milliseconds
   * @param time time information as string
   * @return milliseconds
   */
    def timeToNumber(time:String):Long = {
      try {
        val Array(hour, minute, second, mili) = time.split("[,:.]");
        hour.replaceAll(" ","").toLong*3600*1000+
          minute.replaceAll(" ","").toLong*60*1000+
          second.replaceAll(" ","").toLong*1000+
          mili.replaceAll(" ","").toLong
      } catch {
        case e:Exception=> 0L
      }
    }

  /**
   * Counts distance of two subtitles
   * @param line1 first chunk
   * @param line2 second chunk
   * @return  distance of the chunks
   */
    def linesDistance(line1: UnprocessedChunk, line2:UnprocessedChunk):Long = {
        val start1 = timeToNumber(line1.getStartTime)
        val start2 = timeToNumber(line2.getStartTime)
        val end1 = timeToNumber(line1.getEndTime)
        val end2 = timeToNumber(line2.getEndTime)

        return timeDistance(start1, start2, end1, end2)
    }

  /**
   * Counts the two subtitle files
   * @param file1 one subtitle file
   * @param file2 second subtitle file
   * @param cleanRight Should I try to clean multiple alignings of left-side to right-side?
   *                   (it's faster if I don't, but it's not entirely correct)
   * @param stopAt If non-zero, tells me at which distance sum should I stop with counting
   *               (saves time if I sant the file pair with smallest total sum and I am already past that)
   * @return pair of total sum of distances and pairs of aligned chunks
   */
    def countFiles(file1:SubtitleFile, file2:SubtitleFile, cleanRight:Boolean,stopAt:Long=0):Pair[Long, Seq[Pair[UnprocessedChunk, UnprocessedChunk]]] = {
        countChunks(file1.readChunks, file2.readChunks,cleanRight,stopAt);

    }

  /**
   * Counts the sequences of chunks from two files
   * @param chunksLeft sequence of chunks from one subtitle file
   * @param chunksRight sequence of chunks from second subtitle file
   * @param cleanRight Should I try to clean multiple alignings of left-side to right-side?
   *                   (it's faster if I don't, but it's not entirely correct)
   * @param stopAt If non-zero, tells me at which distance sum should I stop with counting
   *               (saves time if I sant the file pair with smallest total sum and I am already past that)
   * @return pair of total sum of distances and pairs of aligned chunks
   */
    def countChunks(chunksLeft:Seq[UnprocessedChunk], chunksRight:Seq[UnprocessedChunk],cleanRight:Boolean, stopAt:Long=0):Pair[Long, Seq[Pair[UnprocessedChunk, UnprocessedChunk]]] = {
        val tuples = if (cleanRight) {
            removeDuplicateRightAlingment(getBestForEachLeft(chunksLeft, chunksRight));        
        } else {
            getBestForEachLeft(chunksLeft,chunksRight,stopAt);
        }
        var sum = 0L;

        val resultSeq = tuples.map {
            t =>
                sum +=t._1;
                (t._2,t._3)
        }

        return (sum, resultSeq);

    }

  /**
   * Aligns the chunks so that for each chunk on the left there is chunk on the right
   * with shortest distance.
   * It can happen (and it does often) that on two different chunks on the left are aligned
   * with the same chunk on the right
   * @param chunksLeft  sequence of chunks from one subtitle file
   * @param chunksRight  sequence of chunks from second subtitle file
   * @param stopAt  If non-zero, tells me at which distance sum should I stop with counting
   *               (saves time if I sant the file pair with smallest total sum and I am already past that)
   * @return  sequence of distances and pairs of chunks
   */
    def getBestForEachLeft(chunksLeft:Seq[UnprocessedChunk], chunksRight:Seq[UnprocessedChunk],stopAt:Long=0): Seq[Tuple3[Long,UnprocessedChunk, UnprocessedChunk]] = {

       
        var counter = 0L;
        var rightPointer = 0;
        return chunksLeft.flatMap {
            chunkLeft=>
             if (stopAt==0 || counter < stopAt) {
                val distance = linesDistance(chunkLeft, chunksRight(rightPointer));
                while (rightPointer < chunksRight.size-1 && distance > linesDistance(chunkLeft, chunksRight(rightPointer+1))) {
                    rightPointer= rightPointer+1
                }
                val newDistance = linesDistance(chunkLeft, chunksRight(rightPointer))
                counter = counter + newDistance
                Some((newDistance, chunkLeft, chunksRight(rightPointer)))
             } else {
                None
             }
        }
    }

  /**
   * If there is any chunk on right side that has more chunks aligned from left side, selects the
   * one that has the shortest distance
   * @param pairs sequence of distances and pairs of chunks
   * @return sequence of distances and pairs of chunks
   */
    def removeDuplicateRightAlingment(pairs:Seq[Tuple3[Long, UnprocessedChunk, UnprocessedChunk]]):Seq[Tuple3[Long,UnprocessedChunk, UnprocessedChunk]] = {
        val res = new ListBuffer[Tuple3[Long, UnprocessedChunk, UnprocessedChunk]]()
        val buf = new ListBuffer[Pair[Long, UnprocessedChunk]]()
        var lastRightChunk = pairs(0)._2
        pairs.foreach {
           pair =>
            val distance = pair._1
            val leftChunk = pair._2
            val rightChunk = pair._3
            if (!lastRightChunk.equalsTo(rightChunk)) {
                //select the best from buffer
                val bestLeftChunkPair = (buf.min(Ordering.by[(Long,UnprocessedChunk), Long](_._1)))
                buf.clear()
                res.append((bestLeftChunkPair._1, bestLeftChunkPair._2, lastRightChunk))
            }
            buf.append(Pair(distance, leftChunk))
            lastRightChunk = rightChunk;                
        }
        val bestLeftChunkPair = (buf.min(Ordering.by[(Long,UnprocessedChunk), Long](_._1)))
                
        res.append((bestLeftChunkPair._1, bestLeftChunkPair._2, lastRightChunk))

        return res
    
    }


}
