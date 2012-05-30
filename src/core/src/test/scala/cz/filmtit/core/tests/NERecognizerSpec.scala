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

    val personNER = Factory.createNERecognizer(Language.EN, AnnotationType.PERSON, configuration)
    val placeNER =  Factory.createNERecognizer(Language.EN, AnnotationType.PLACE, configuration)


    it("should add annotations") {
      val chunk: Chunk = "My name is Peter Fonda and I work for IBM Inc. in New York."
      personNER.detect(chunk)
      assert( chunk.getAnnotations.size > 0 )
    }

    it("should find the Person and Place in this sentence") {
      val chunk: Chunk = "My name is Peter Fonda and I work for IBM Inc. in New York"
      personNER.detect(chunk)
      placeNER.detect(chunk)
      println(chunk.toAnnotatedString())
      assertEquals(
        "My name is <Person> and I work for IBM Inc. in <Place>",
        chunk.toAnnotatedString()
      )
    }



  }


}
