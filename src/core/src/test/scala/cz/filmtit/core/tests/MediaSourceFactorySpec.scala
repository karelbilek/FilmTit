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

import java.util
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


import org.scalatest.Spec
import cz.filmtit.share.MediaSource
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory


/**
 * Test specification for the data model classes.
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class MediaSourceFactorySpec extends Spec {

  val msFactory = new FreebaseMediaSourceFactory()

  describe("A MediaSourceFactory") {

    it("should find entries for very popular movies") {
      assert(msFactory.getSuggestions("Matrix").size() > 0)
    }

    it("should find the best entry for a very popular movie") {
      assert(msFactory.getSuggestion("Matrix", "1999").getTitle.equals("The Matrix"))
    }

    it("should find entries for not so popular movies") {
      val jaws: util.List[MediaSource] = msFactory.getSuggestions("Jaws")
      assert(jaws.size() > 0)
    }

  }
}
