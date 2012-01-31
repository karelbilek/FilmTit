package cz.filmtit.core.model

import cz.filmtit.core.model.Language._
import com.weiglewilczek.slf4s.{Logger, Logging}

/**
 * @author Joachim Daiber
 *
 */

class TranslationMemory(val storage: TranslationPairStorage, val ranker: TranslationPairRanker) extends Logging {

  def nBest(chunk: Chunk, mediaSource: MediaSource, language: Language, n: Int = 10): List[ScoredTranslationPair] = {
    var s = System.currentTimeMillis
    val candidates = storage.candidates(chunk, language)
    logger.info( "Retrieved %d candiates in %dms...".format(candidates.size, System.currentTimeMillis - s) )

    s = System.currentTimeMillis
    val ranked = ranker.rank(chunk, null, candidates)
    logger.info( "Ranking candiates took %dms...".format(System.currentTimeMillis - s) )

    ranked.take(n)
  }

  def firstBest(chunk: Chunk, mediaSource: MediaSource, language: Language):
    TranslationPair = nBest(chunk, mediaSource, language).head


}

