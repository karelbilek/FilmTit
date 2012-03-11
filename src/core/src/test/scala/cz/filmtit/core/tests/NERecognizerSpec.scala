package cz.filmtit.core.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import junit.framework.Assert._

import org.scalatest.Spec
import cz.filmtit.core.Factory
import cz.filmtit.core.model.Language
import cz.filmtit.core.model.data.Chunk
import cz.filmtit.core.model.annotation.Name


/**
 * Test specification for NE recognizers.
 *
 * @author Joachim Daiber
 */


@RunWith(classOf[JUnitRunner])
class NERecognizerSpec extends Spec {

  describe("A NER") {

    val personNER = Factory.createNERecognizer(Language.en, Name.Person)
    val placeNER =  Factory.createNERecognizer(Language.en, Name.Place)


    it("should add annotations") {
      val chunk: Chunk = "My name is Peter Fonda and I work for IBM Inc. in New York."
      personNER.detect(chunk)
      assert( chunk.annotations.size > 0 )
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
