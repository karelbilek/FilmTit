/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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

  override def toString = name

}
