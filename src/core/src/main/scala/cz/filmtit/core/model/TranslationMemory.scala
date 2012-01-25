package cz.filmtit.core.model

import cz.filmtit.core.model.Language._

/**
 * @author Joachim Daiber
 *
 *
 *
 */

class TranslationMemory(val storage: TranslationPairStorage, val ranker: TranslationPairRanker) {

  def nBest(chunk: Chunk, mediaSource: MediaSource, language: Language): List[ScoredTranslationPair] = {
    ranker.rank(chunk, null, storage.candidates(chunk, language))
  }

  def firstBest(chunk: Chunk, mediaSource: MediaSource, language: Language):
    TranslationPair = nBest(chunk, mediaSource, language).head


}

