package cz.filmtit.dataimport.alignment.aligners.trivial

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk


/**
 * Class for trivial chunk<->chunk alignment
 * (just take first with first, second with second etc)
 *
 * @constructor create a new aligner.
 * @param l1 first language
 * @param l2 second language
 */
class TrivialChunkAlignment(l1:Language, l2:Language) extends ChunkAlignment(l1, l2) {

  /**
   * Trivial chunk aligning
   * @param chunksL1 chunks of one file
   * @param chunksL2 chunks of second file
   * @return pairs of aligned chunks with no real aligning (0 with 0, 1 with 1 and so on)
   */
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]] = {
        val result:ListBuffer[Pair[UnprocessedChunk, UnprocessedChunk]] = new ListBuffer[Pair[UnprocessedChunk, UnprocessedChunk]] ()
        val min = Math.min(chunksL1.size, chunksL2.size);
        

        (0 to min-1).foreach {
            i=>result.append((chunksL1(i), chunksL2(i)))
        }

        return result.toList
    }
    

}
