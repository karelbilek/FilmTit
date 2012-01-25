package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 */

trait TranslationPairRanker {

  def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]): List[ScoredTranslationPair]

  def name: String

}

