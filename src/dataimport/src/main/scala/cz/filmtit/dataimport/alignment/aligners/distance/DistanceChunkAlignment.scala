package cz.filmtit.dataimport.alignment.aligners.distance

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk


class DistanceChunkAlignment(l1:Language, l2:Language, val counter: FilePairCounter) extends ChunkAlignment(l1, l2) {
    
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]] = {
        
        return counter.countChunks(chunksL1, chunksL2,true)._2.toList; 
    }
    

}
