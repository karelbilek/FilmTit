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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.core.search.external.MyMemorySearcher
import cz.filmtit.share.Language
import cz.filmtit.core.Utils.chunkFromString

/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class SearcherSpec extends Spec {

  val searchers = List(
    new MyMemorySearcher(Language.EN, Language.CS)
  )

  describe("A Searcher") {
    it("should return a translation for a sentence") {
      searchers.foreach(s =>
        assert(s.candidates("My name is George Bush.", Language.EN).size > 0)
      )
    }
  }


}
