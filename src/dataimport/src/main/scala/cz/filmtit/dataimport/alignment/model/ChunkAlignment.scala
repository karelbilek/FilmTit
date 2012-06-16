package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk

/**
 * An abstract object for aligning chunk to chunk
 *
 * @constructor create a new chunkAlignment
 * @param l1 first language
 * @param l2 second language
 */
abstract class ChunkAlignment(val l1:Language, val l2:Language)  {

  /**
   * Abstract method that aligns cunk to chunk
   * @param chunksL1 chunks of one file
   * @param chunksL2 chunks of second file
   * @return pairs of aligned chuns
   */
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]];
    

}
