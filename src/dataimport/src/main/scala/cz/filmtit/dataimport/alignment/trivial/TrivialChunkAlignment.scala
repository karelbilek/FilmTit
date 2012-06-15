package cz.filmtit.dataimport.alignment.trivial

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk


class TrivialChunkAlignment(l1:Language, l2:Language) extends ChunkAlignment(l1, l2) {
    
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]] = {
        val result:ListBuffer[Pair[UnprocessedChunk, UnprocessedChunk]] = new ListBuffer[Pair[UnprocessedChunk, UnprocessedChunk]] ()
        val min = Math.min(chunksL1.size, chunksL2.size);
        (0 to min-1).foreach {
            i=>result.append((chunksL1(i), chunksL2(i)))
        }
        return result.toList
    }
    

}
