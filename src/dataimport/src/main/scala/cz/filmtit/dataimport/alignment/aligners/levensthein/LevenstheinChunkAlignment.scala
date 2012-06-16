package cz.filmtit.dataimport.alignment.aligners.levensthein

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.SubHelper.timeToNumber


class LevenstheinChunkAlignment(l1:Language, l2:Language, tolerance:Long) extends ChunkAlignment(l1, l2) {


    abstract sealed class ComparisonResult
    case object Match extends ComparisonResult
    case object EndMismatch extends ComparisonResult
    case object FirstLate extends ComparisonResult
    case object SecondLate extends ComparisonResult

    def compareChunks(ch1:UnprocessedChunk, ch2:UnprocessedChunk):ComparisonResult = {
        val start1 = timeToNumber(ch1.getStartTime)
        val end1 = timeToNumber(ch1.getEndTime)
        val start2 = timeToNumber(ch2.getStartTime)
        val end2 = timeToNumber(ch2.getEndTime)
        
        import Math.abs

        if(abs(start1-start2)<= tolerance) {
            if (abs(end1-end2) <= tolerance) {
                Match
            } else {
                EndMismatch
            }
        } else {
            if (start1 > start2+ tolerance) {
                FirstLate
            } else {
                SecondLate
            }
        }
    }
    
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]] = {
        val result:ListBuffer[Pair[UnprocessedChunk, UnprocessedChunk]] = new ListBuffer[Pair[UnprocessedChunk, UnprocessedChunk]] ()
        
        var l1pointer = 0;
        var l2pointer = 0;
        while (l1pointer < chunksL1.size && l2pointer < chunksL2.size) {
            val ch1 = chunksL1(l1pointer)
            val ch2 = chunksL2(l2pointer)
            val comparisn = compareChunks(ch1, ch2)
            comparisn match {
                case Match => result.append((ch1, ch2));l1pointer += 1; l2pointer += 1;
                case EndMismatch=> l1pointer +=1 ; l2pointer += 1;
                case FirstLate=>l1pointer+=1
                case SecondLate=>l2pointer+=1
            }
        }
        
        return result.toList
    }
    

}
