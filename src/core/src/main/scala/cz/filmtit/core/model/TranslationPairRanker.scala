package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 */

abstract class TranslationPairRanker {

  def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
        List[ScoredTranslationPair] = pairs.map(rankOne).sorted

  def best(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
        ScoredTranslationPair = pairs.map(rankOne).max

  def rankOne(pair: TranslationPair): ScoredTranslationPair

  def name: String

}
