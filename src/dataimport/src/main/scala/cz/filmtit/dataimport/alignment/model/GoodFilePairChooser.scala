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

package cz.filmtit.dataimport.alignment.model

import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * An abstract object for determining whether to take file pairs that
 * are already aligned
 *
 */
abstract class GoodFilePairChooser {
  /**
   * Abstract method determining which file pairs to take
   * @param pairs all file pairs
   * @return the correct file pairs
   */
    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):Iterable[Pair[SubtitleFile, SubtitleFile]];

}
