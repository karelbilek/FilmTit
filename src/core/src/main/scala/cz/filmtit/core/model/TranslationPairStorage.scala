package cz.filmtit.core.model

/**
 *
 *
 * @author Joachim Daiber
 */
trait TranslationPairStorage {

  def candidates(sentence: Chunk): List[ScoredTranslationPair]
  def initialize(translationPairs: TraversableOnce[TranslationPair])
  def addTranslationPair(translationPair: TranslationPair)

  def name: String

}

trait SignatureBasedStorage extends TranslationPairStorage {

  def signature(sentence: Chunk): String

}
