package cz.filmtit.core.model
import cz.filmtit.core.model.Language._


/** Interface for retrieving translation pair candidates from a database.
  *
  * @author Joachim Daiber
  */
abstract class TranslationPairStorage(val l1: Language, val l2: Language) {

  def chunk(pair: TranslationPair, language: Language): Chunk = {
    if (language equals l1)
      pair.source
    else
      pair.target
  }

  /** Retrieve a list of candidate translation pairs from the database.
    * Depending on the implementation, the pairs may have a
    * [[cz.filmtit.core.model.ScoredTranslationPair#canidateScore]]
    */
  def candidates(chunk: Chunk, language: Language): List[TranslationPair]

  /** Create a new database with the initial set of translation pairs. */
  def initialize(translationPairs: TraversableOnce[TranslationPair])

  /** Reload indexes */
  def reindex()

  /** Add a single translation pair to the database */
  def addTranslationPair(translationPair: TranslationPair)

  def addMediaSource(mediaSource: MediaSource): Long

  /** Short description of the implementation */
  def name: String

}
