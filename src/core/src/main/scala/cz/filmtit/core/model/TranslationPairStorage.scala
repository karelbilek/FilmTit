package cz.filmtit.core.model

/** Interface for retrieving translation pair candidates from a database.
  *
  * @author Joachim Daiber
  */
trait TranslationPairStorage {

  /** Retrieve a list of candidate translation pairs from the database.
    * Depending on the implementation, the pairs may have a
    * [[ScoredTranslationPair#canidateScore]]
    */
  def candidates(sentence: Chunk): List[ScoredTranslationPair]

  /** Create a new empty database. */
  def initialize()

  /** Create a new database with the initial set of translation pairs. */
  def initialize(translationPairs: TraversableOnce[TranslationPair])

  /** Add a single translation pair to the database */
  def addTranslationPair(translationPair: TranslationPair)

  /** Short description of the implementation */
  def name: String

}
