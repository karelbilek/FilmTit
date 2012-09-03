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

package cz.filmtit.dataimport.database

import cz.filmtit.core.{Configuration, Factory}
import java.io.File
import cz.filmtit.core.model.TranslationMemory


/**
 * @author Joachim Daiber
 */

object Reindex {
  def main(args: Array[String]) {
    val configuration = new Configuration(new File(args(0)))

    val tm: TranslationMemory = Factory.createTM(
          configuration.l1, configuration.l2,
          Factory.createConnection(configuration, readOnly=false),
          configuration,
          1,
          configuration.searcherTimeout,
          indexing=true
    )

    tm.reindex()
    tm.close()
  }
}
