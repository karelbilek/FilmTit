package cz.filmtit.core.model.storage

import cz.filmtit.core.model._
import cz.filmtit.core.model.data.{Chunk, TranslationPair}
import cz.filmtit.core.model.Language

/** Interface for retrieving translation pair candidates from a database.
 *
 * @author Joachim Daiber
 */
abstract class TranslationPairStorage(l1: Language, l2: Language)
extends TranslationPairSearcher(l1, l2) {

  def chunkForLanguage(pair: TranslationPair, language: Language): Chunk = {
    if (language equals l1)
      pair.source
    else
      pair.target
  }

  /** Reset the database  */
  def reset()

  /**Create a new database with the initial set of translation pairs. */
  def initialize(translationPairs: TraversableOnce[TranslationPair])

  /**Reload indexes */
  def reindex()

  /**Add a single translation pair to the database */
  def addTranslationPair(translationPair: TranslationPair)

  /**Short description of the implementation */
  def name: String

}
