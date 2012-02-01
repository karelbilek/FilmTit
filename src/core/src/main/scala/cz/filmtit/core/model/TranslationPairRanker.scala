package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 */

abstract class TranslationPairRanker {

  def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
    List[ScoredTranslationPair]
    = pairs.map(pair => rankOne(chunk, mediaSource,pair)).sorted

  def best(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
    Option[ScoredTranslationPair] = {

    pairs.map(pair => rankOne(chunk, mediaSource,pair)) match {
      case List()                                  => None
      case x: List[Ordered[ScoredTranslationPair]] => Some(x.max)
    }
  }

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair):
    ScoredTranslationPair

  def name: String

}
