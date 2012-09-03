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

package cz.filmtit.core.tm

import cz.filmtit.core.model.{TranslationPairRanker, TranslationPairSearcher}
import cz.filmtit.core.concurrency.searcher.TranslationPairSearcherWrapper
import cz.filmtit.share.TranslationSource

/**
 * A back off level in a [[cz.filmtit.core.tm.BackoffTranslationMemory]]. On the level, candidates are retrieved
 * using the searcher object, ranked using the ranker object and used if their score is above the threshold value.
 *
 * @author Joachim Daiber
 */

class BackoffLevel(val searcher: TranslationPairSearcher, val ranker: Option[TranslationPairRanker], val threshold: Double, val translationType: TranslationSource) {

  override def toString = "[%s, %s]".format(
      searcher match {
        case s: TranslationPairSearcherWrapper => { "%s (%d concurrent instances)".format(s.searchers.head.getClass.getSimpleName, s.size) }
        case s: TranslationPairSearcher => s.getClass.getSimpleName
      },
      ranker match {
        case Some(r) => r.getClass.getSimpleName
        case None => "no ranker"
      }
  )

}