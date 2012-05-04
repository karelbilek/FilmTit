package cz.filmtit.core.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest.Spec
import cz.filmtit.core.model.data.AnnotatedChunk
import cz.filmtit.core.model.annotation.Name

import junit.framework.Assert.assertEquals
import cz.filmtit.share.Language
import cz.filmtit.core.{Configuration, Factory}
import java.io.File


/**
 * Test specification for NE recognizers.
 *
 * @author Joachim Daiber
 */


@RunWith(classOf[JUnitRunner])
class NERecognizerSpec extends Spec {
  val configuration = new Configuration(new File("configuration.xml"))

  describe("A NER") {

    val personNER = Factory.createNERecognizer(Language.EN, Name.Person, configuration)
    val placeNER =  Factory.createNERecognizer(Language.EN, Name.Place, configuration)


    it("should add annotations") {
      val chunk: AnnotatedChunk = "My name is Peter Fonda and I work for IBM Inc. in New York."
      personNER.detect(chunk)
      assert( chunk.annotations.size > 0 )
    }

    it("should find the Person and Place in this sentence") {
      val chunk: AnnotatedChunk = "My name is Peter Fonda and I work for IBM Inc. in New York"
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
