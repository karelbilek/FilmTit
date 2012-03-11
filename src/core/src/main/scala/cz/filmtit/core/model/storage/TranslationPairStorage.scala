package cz.filmtit.core.model.storage

import cz.filmtit.core.model._
import cz.filmtit.core.model.data.TranslationPair
import cz.filmtit.core.model.Language

/**
 * Interface for retrieving translation pair candidates from a database.
 *
 * @author Joachim Daiber
 */
abstract class TranslationPairStorage(l1: Language, l2: Language)
extends TranslationPairSearcher(l1, l2) {

  /** Reset the database  */
  def reset()

  /**Add the translation pairs to the database. */
  def add(translationPairs: TraversableOnce[TranslationPair])

  /**Reload indexes */
  def reindex()

  /**Short description of the implementation */
  def name: String

}
