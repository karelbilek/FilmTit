package cz.filmtit.dataimport.alignment.aligners.distance

import scala.collection.mutable.ListBuffer
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.model.SubtitleFile

abstract class FilePairCounter {

    def timeDistance(start1:Long, start2:Long, end1:Long, end2:Long):Long

    def timeToNumber(time:String):Long = {
      try {
        val Array(hour, minute, second, mili) = time.split("[,:.]");
        hour.replaceAll(" ","").toLong*3600*1000+minute.replaceAll(" ","").toLong*60*1000+second.replaceAll(" ","").toLong*1000+mili.replaceAll(" ","").toLong
      } catch {
        case e:Exception=> 0L
      }
    }

    def linesDistance(line1: UnprocessedChunk, line2:UnprocessedChunk):Long = {
        val start1 = timeToNumber(line1.getStartTime)
        val start2 = timeToNumber(line2.getStartTime)
        val end1 = timeToNumber(line1.getEndTime)
        val end2 = timeToNumber(line2.getEndTime)

        return timeDistance(start1, start2, end1, end2)
    }
    
    def countFiles(file1:SubtitleFile, file2:SubtitleFile, cleanRight:Boolean,stopAt:Long=0):Pair[Long, Seq[Pair[UnprocessedChunk, UnprocessedChunk]]] = {
        countChunks(file1.readChunks, file2.readChunks,cleanRight,stopAt);

    }

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
