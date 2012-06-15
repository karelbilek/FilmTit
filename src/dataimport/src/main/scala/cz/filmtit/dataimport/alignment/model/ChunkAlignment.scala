package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk

abstract class ChunkAlignment(val l1:Language, val l2:Language)  {
    
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]];
    

}
