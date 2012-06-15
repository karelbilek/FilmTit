package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk

abstract class ChunkAlignment(val l1:Language, val l2:Language) {
    
    def alignChunks(fileL1: SubtitleFile, fileL2:SubtitleFile):List[Pair[UnprocessedChunk, UnprocessedChunk]];
    

}
