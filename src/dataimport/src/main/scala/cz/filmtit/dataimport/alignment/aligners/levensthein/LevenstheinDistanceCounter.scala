package cz.filmtit.dataimport.alignment.aligners.levensthein

import scala.collection.mutable.HashMap
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.SubHelper.timeToNumber
import cz.filmtit.dataimport.alignment.io.SubtitleFile

class LevenstheinDistanceCounter(val tolerance:Long) {

    def count(f1:SubtitleFile, f2:SubtitleFile):Int = {
        count(f1.readChunks, f2.readChunks)
    }

    def chunksToLongs(f:Seq[UnprocessedChunk]):Seq[Long] = {
        f.flatMap {
          chunk=> List(timeToNumber(chunk.getStartTime), timeToNumber(chunk.getEndTime))
        }
    }

    def count(chunks1:Seq[UnprocessedChunk], chunks2:Seq[UnprocessedChunk]):Int = {
        val shorter1 = chunksToLongs(chunks1).take(100)
        val shorter2 = chunksToLongs(chunks2).take(100)
        return countDistance(shorter1, shorter2)
    }

    def countDistance(times1:Seq[Long], times2:Seq[Long]):Int = {
        import Math._

        val table = new HashMap[Pair[Int,Int], Int]()

        (0 to times1.size).foreach{i=>table((i, 0))=i}
        (0 to times2.size).foreach{i=>table((0, i))=i}

        (1 to times1.size).foreach{
          i=>
            (1 to times2.size).foreach{
              j=>
                if (abs(times1(i-1) - times2(j-1)) < tolerance) {
                    table((i,j)) = table((i-1, j-1))
                } else {
                    table((i,j)) = min(min(table((i-1, j)), table((i,j-1))), table((i-1,j-1)))+1
                }
            }
        }
        return table(times1.size, times2.size)
    }

}
