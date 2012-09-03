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
import junit.framework.Assert._


import org.scalatest.Spec
import cz.filmtit.share.Chunk
import cz.filmtit.share.annotations._
import cz.filmtit.core.model.data.ChunkUtils.toAnnotatedString
import cz.filmtit.core.Utils.chunkFromString


/**
 * Test specification for the data model classes.
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class DataSpec extends Spec {

  describe("A Chunk") {

    it("can be created from a String") {
      assert( new Chunk("test") equals new Chunk("test") )
    }

    val chunk: Chunk = "I am Jo"

    it("can have annotations") {
      chunk.addAnnotation(new Annotation(AnnotationType.PERSON, 5, 7))
      assert(chunk.getAnnotations.size > 0)
    }

    it("can be printed with its annotations") {

        //(needs implicit conversion with ChunkUtil)
      //Show only type:
      assertEquals(
        "I am <Person>",
        toAnnotatedString(chunk)
      )

      //Show type and surface form:
      assertEquals(
        "I am <Person:Jo>",
        toAnnotatedString(chunk, {(tag, surface) => "<%s:%s>".format(tag.getDescription, surface)})
      )

    }

    it("can have many annotations") {

      val chunk: Chunk = "Peter lives in New York with Gabi"
      chunk.addAnnotation(new Annotation(AnnotationType.PERSON, 0, 5))
      chunk.addAnnotation(new Annotation(AnnotationType.PLACE, 15, 23))
      chunk.addAnnotation(new Annotation(AnnotationType.PERSON, 29, 33))

      assertEquals(
        "<Person> lives in <Place> with <Person>",
        toAnnotatedString(chunk, {(ann, string) => "<%s>".format(ann.getDescription)})
      )

    }

  }


}
