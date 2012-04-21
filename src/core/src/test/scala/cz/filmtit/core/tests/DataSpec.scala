package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import junit.framework.Assert._


import org.scalatest.Spec
import cz.filmtit.core.model.annotation.Name
import cz.filmtit.core.model.data.AnnotatedChunk
import cz.filmtit.share.Chunk

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

    val chunk: AnnotatedChunk = "I am Jo"

    it("can have annotations") {
      chunk.addAnnotation(Name.Person, 5, 7)
      assert(chunk.annotations.size > 0)
    }

    it("can be printed with its annotations") {

      //Show only type:
      assertEquals(
        "I am <Person>",
        chunk.toAnnotatedString()
      )

      //Show type and surface form:
      assertEquals(
        "I am <Person:Jo>",
        chunk.toAnnotatedString((tag, surface) => "<%s:%s>".format(tag, surface))
      )

    }

    it("can have many annotations") {

      val chunk: AnnotatedChunk = "Peter lives in New York with Gabi"
      chunk.addAnnotation(Name.Person, 0, 5)
      chunk.addAnnotation(Name.Place, 15, 23)
      chunk.addAnnotation(Name.Person, 29, 33)

      assertEquals(
        "<Person> lives in <Place> with <Person>",
        chunk.toAnnotatedString((ann, string) => "<%s>".format(ann))
      )

    }

  }


}
