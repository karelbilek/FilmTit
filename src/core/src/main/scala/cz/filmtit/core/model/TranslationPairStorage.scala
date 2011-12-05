package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 *
 *
 *
 */

trait TranslationPairStorage {

  def candidates(sentence: String): List[ScoredTranslationPair]
  def initialize(translationPairs: TraversableOnce[TranslationPair])
  def addTranslationPair(translationPair: TranslationPair)

  def name(): String


}