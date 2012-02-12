package cz.filmtit.core.rank

import cz.filmtit.core.model.TranslationPairRanker
import cz.filmtit.core.model.data.{ScoredTranslationPair, Chunk, MediaSource, TranslationPair}
import collection.mutable.ListBuffer
import cz.filmtit.core.model.annotation.ChunkAnnotation


/**
 * @author Joachim Daiber
 *
 *
 *
 */

class FuzzyNERanker extends TranslationPairRanker {

  /**
   * NE-based translation pairs should be ranked by their similarity
   * to the searched chunk. The best possible match differs only in
   * 1 NE, the next best in 2, etc.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pair the translation pair candidate
   * @return translation pair with score
   */
  def rankOne(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair):
  ScoredTranslationPair = {

    /**
     * We know that the two strings have the same NE annotations and that
     * the rests of the strings are equal.
     *
     * Check whether despite the NEs, the underling surface forms are also
     * the same.
     */
    val matchingNESurfaces = 0
    for (i <- (0 until pair.source.annotations.size)) {
      val (_, aPF, aPT): (ChunkAnnotation, Int, Int) = pair.source.annotations(i)
      val (_, aCF, aCT): (ChunkAnnotation, Int, Int) = chunk.annotations(i)

      if (pair.source.surfaceform.substring(aPF, aPT) equals
        chunk.surfaceform.substring(aCF, aCT))
        matchingNESurfaces += 1
    }

    ScoredTranslationPair.fromTranslationPair(pair, matchingNESurfaces)
  }

  def name = "Fuzzy ranker for Named Entities."

}
