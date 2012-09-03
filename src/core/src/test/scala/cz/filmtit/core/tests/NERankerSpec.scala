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
import cz.filmtit.core.model.names.NERecognizer
import cz.filmtit.core.rank.FuzzyNERanker
import org.junit.Assert.assertEquals
import cz.filmtit.share._
import cz.filmtit.share.annotations._
import cz.filmtit.core.{Configuration, Factory}
import java.io.File
import cz.filmtit.core.model.data.ChunkUtils._
import cz.filmtit.core.Utils.chunkFromString



/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class NERankerSpec extends Spec {

  val configuration = new Configuration(new File("configuration.xml"))

  val recognizer: NERecognizer = Factory.createNERecognizer(Language.EN, AnnotationType.PERSON, configuration, Factory.createTokenizerWrapper(Language.EN, configuration))
  val ranker: FuzzyNERanker = new FuzzyNERanker()


  describe("A fuzzy NE ranker") {
    it("must be able to correctly count the number of matching surface forms") {

      val c1: Chunk = "Peter saw Thomas on the street."
      val c2: Chunk = "Thomas saw Thomas on the street."
      val c3: Chunk = "Thomas saw Peter on the street."

      recognizer.detect(c1)
      recognizer.detect(c2)
      recognizer.detect(c3)

      assertEquals(1, ranker.matchingSFs(c1, c2).size)
      assertEquals(0, ranker.matchingSFs(c1, c3).size)
    }
  }


}
