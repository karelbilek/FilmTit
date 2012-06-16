package cz.filmtit.dataimport.alignment.aligners.distance

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk

/**
 * A class that aligns the best chunks together
 *
 * @constructor create a new DistanceChunkAlignment.
 * @param l1 first language
 * @param l2 second language
 * @param counter  how to count the distances
 */
class DistanceChunkAlignment(l1:Language, l2:Language, val counter: FilePairCounter) extends ChunkAlignment(l1, l2) {

  /**
   * Align the bestchunks together by FilePairCounter
   * @param chunksL1 chunks of one file
   * @param chunksL2 chunks of second file
   * @return pairs of aligned chuns
   */
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]] = {
        
        return counter.countChunks(chunksL1, chunksL2,true)._2.toList; 
    }
    

}
