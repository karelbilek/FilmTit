package cz.filmtit.core.rank

import cz.filmtit.core.model.{TranslationPair, MediaSource, Chunk, TranslationPairRanker}


/**
 * @author Joachim Daiber
 *
 *
 *
 */

class FuzzyNERanker extends TranslationPairRanker {

  def rankOne(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair) = null

  def name = "Fuzzy ranker for Named Entities."

}
