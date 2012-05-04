package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.core.model.annotation.Name
import cz.filmtit.core.model.names.NERecognizer
import cz.filmtit.core.model.data.AnnotatedChunk
import cz.filmtit.core.rank.FuzzyNERanker
import org.junit.Assert.assertEquals
import cz.filmtit.share.Language
import cz.filmtit.core.{Configuration, Factory}
import java.io.File


/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class FuzzyNERankerSpec extends Spec {

  val configuration = new Configuration(new File("configuration.xml"))

  val recognizer: NERecognizer = Factory.createNERecognizer(Language.EN, Name.Person, configuration)
  val ranker: FuzzyNERanker = new FuzzyNERanker()


  describe("A fuzzy NE ranker") {
    it("must be able to correctly count the number of matching surface forms") {

      val c1: AnnotatedChunk = "Peter saw Thomas on the street."
      val c2: AnnotatedChunk = "Thomas saw Thomas on the street."
      val c3: AnnotatedChunk = "Thomas saw Peter on the street."

      recognizer.detect(c1)
      recognizer.detect(c2)
      recognizer.detect(c3)

      assertEquals(1, ranker.matchingSFs(c1, c2).size)
      assertEquals(0, ranker.matchingSFs(c1, c3).size)
    }
  }


}
