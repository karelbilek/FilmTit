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

package cz.filmtit.core.model

import cz.filmtit.share.TranslationPair

/**
 * Interface for merging translation pairs. Translation pairs should be merged if they are too similar.
 *
 * @author Joachim Daiber
 */

abstract class TranslationPairMerger {

  /**
   * Merge translation pairs such that pairs that are too similar are merged into one pair.
   *
   * This step should be performed after the translation pairs were ranked by a [[cz.filmtit.core.model.TranslationPairRanker]].
   *
   * @param pairs the scored translation pairs
   * @param n number of translation pair candidates that should be returned
   * @return the n best translation pairs, with similar pairs merged into a single pair
   */
  def merge(pairs: List[TranslationPair], n: Int): List[TranslationPair]

}