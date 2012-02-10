package cz.filmtit.core.test
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import junit.framework.Assert._


import org.scalatest.Spec
import cz.filmtit.core.model.annotation.Name
import cz.filmtit.core.model.data.Chunk
/**
 * @author Joachim Daiber
 *
 *
 *
 */


@RunWith(classOf[JUnitRunner])
class DataSpec extends Spec {

  describe("A Chunk") {

    it("should be created from a String") {
      assert( Chunk.fromString("test") equals new Chunk("test") )
    }

    val chunk: Chunk = "I am Jo"
    chunk.addAnnotation(Name.Person, 5, 7)

    it("can have annotations") {
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
        chunk.toAnnotatedString((ann, string) => "<%s:%s>".format(ann, string))
      )

    }

    it("can have many annotations") {

      val chunk: Chunk = "Peter lives in New York with Gabi"
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
