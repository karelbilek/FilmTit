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

import junit.framework.Assert.assertEquals
import cz.filmtit.share._
import cz.filmtit.share.annotations._
import cz.filmtit.core.{Configuration, Factory}
import java.io.File
import cz.filmtit.core.Utils.chunkFromString
import cz.filmtit.core.model.data.ChunkUtils._


/**
 * Test specification for NE recognizers.
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class NERecognizerSpec extends Spec {
  val configuration = new Configuration(new File("configuration.xml"))

  describe("A NER") {
    
    val enwrap = Factory.createTokenizerWrapper(Language.EN, configuration)

    val personNER = Factory.createNERecognizer(Language.EN, AnnotationType.PERSON, configuration, enwrap)
    val placeNER =  Factory.createNERecognizer(Language.EN, AnnotationType.PLACE, configuration, enwrap)


    it("should add annotations") {
      val chunk: Chunk = "My name is Peter Fonda and I work for IBM Inc. in New York."
      personNER.detect(chunk)
      assert( chunk.getAnnotations.size > 0 )
    }

    it("should find the Person and Place in this sentence") {
      val chunk: Chunk = "My name is Peter Fonda and I work for IBM Inc. in New York"
      personNER.detect(chunk)
      placeNER.detect(chunk)
      assertEquals(
        "My name is <Person> and I work for IBM Inc. in <Place>",
        toAnnotatedString(chunk)
      )
    }



  }


}
