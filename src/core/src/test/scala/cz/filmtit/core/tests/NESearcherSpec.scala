package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.share.Language
import cz.filmtit.core.search.postgres.impl.NEStorage
import cz.filmtit.core.model.data.AnnotatedChunk
import cz.filmtit.core.Utils.chunkFromString
import cz.filmtit.core.{Configuration, Factory}
import java.io.File

/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class NESearcherSpec extends Spec {

  val configuration = new Configuration(new File("configuration.xml"))

  val recognizers = Factory.defaultNERecognizers(configuration)
  val connection = Factory.createConnection(configuration)

  val searcher = new NEStorage(Language.EN, Language.CS, connection, recognizers._1, recognizers._2)

  describe("A NE searcher") {
    it("should be able to restore the NE in the chunk") {

      val candidates = searcher.candidates("Peter", Language.EN)

      /* Since we found the results via NE matches, the corresponding NE
         annotations must be restorable from the database. */
      assert(
        candidates.exists(_.getChunkL1.asInstanceOf[AnnotatedChunk].toAnnotatedString() contains "<Person>" )
      )
    }
  }


}
