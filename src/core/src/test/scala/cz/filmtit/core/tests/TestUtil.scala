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

package cz.filmtit.core.tests

import scala.Array
import cz.filmtit.share.TranslationPair
import cz.filmtit.core.model.TranslationMemory
import cz.filmtit.core.{Configuration, Factory}

/**
 * Utility methods used in tests related to the Core Translation Memory.
 *
 * @author Joachim Daiber
 */
object TestUtil {

  def createTMWithDummyContent(configuration: Configuration): TranslationMemory = {
    val tm = Factory.createTMFromConfiguration(
      configuration,
      readOnly=false,
      useInMemoryDB=true
    )

    tm.reset()
    tm.add(Array(
      new TranslationPair("Peter rode to Alabama.", "Petr jel do Alabamy."),
      new TranslationPair("Peter rode to Alaska.", "Petr jel do Aljašky.")
    ))
    tm.reindex()
    tm
  }

}
