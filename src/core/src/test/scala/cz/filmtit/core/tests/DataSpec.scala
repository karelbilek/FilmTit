package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import junit.framework.Assert._


import org.scalatest.Spec
import cz.filmtit.share.Chunk
import cz.filmtit.share.annotations._
import cz.filmtit.core.model.data.ChunkUtils._
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
        chunk.toAnnotatedString()
      )

      //Show type and surface form:
      assertEquals(
        "I am <Person:Jo>",
        chunk.toAnnotatedString((tag, surface) => "<%s:%s>".format(tag.getDescription, surface))
      )

    }

    it("can have many annotations") {

      val chunk: Chunk = "Peter lives in New York with Gabi"
      chunk.addAnnotation(new Annotation(AnnotationType.PERSON, 0, 5))
      chunk.addAnnotation(new Annotation(AnnotationType.PLACE, 15, 23))
      chunk.addAnnotation(new Annotation(AnnotationType.PERSON, 29, 33))

      assertEquals(
        "<Person> lives in <Place> with <Person>",
        chunk.toAnnotatedString((ann, string) => "<%s>".format(ann.getDescription))
      )

    }

  }


}
