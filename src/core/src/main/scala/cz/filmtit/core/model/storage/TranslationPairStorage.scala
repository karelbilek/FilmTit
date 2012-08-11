package cz.filmtit.core.model.storage

import cz.filmtit.core.model._
import cz.filmtit.share.{Language, TranslationPair}

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

  /**Finish import */
  def finishImport()

  /**Warmup*/
  def warmup()

  /**Short description of the implementation */
  def name: String

}
