package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 */

trait TranslationPairRanker {

  def rank(pairs: List[TranslationPair]): List[ScoredTranslationPair]

  def name: String

}

