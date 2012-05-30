package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.share._
import cz.filmtit.core.search.postgres.impl.NEStorage
import cz.filmtit.core.Utils.chunkFromString
import cz.filmtit.core.{Configuration, Factory}
import java.io.File
import TestUtil.createTMWithDummyContent
import cz.filmtit.core.model.data.ChunkUtils.toAnnotatedString
import cz.filmtit.core.Utils.chunkFromString

/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class NESearcherSpec extends Spec {

  val configuration = new Configuration(new File("configuration.xml"))
  configuration.maxNumberOfConcurrentSearchers = 10
  val memory = createTMWithDummyContent(configuration)

  describe("A NE searcher") {
    it("should be able to restore the NE in the chunk") {

      val candidates = memory.firstBest("Thomas rode to Alabama", Language.EN, null)

      /* Since we found the results via NE matches, the corresponding NE
         annotations must be restorable from the database. */
      assert(
        candidates.exists(pair => toAnnotatedString(pair.getChunkL1) contains "<Person>" )
      )
    }

    it("should be queryable by multiple threads at the same time") {

      //Query the same TM from n threads in parallel:
      (1 to 500).par foreach { _ =>
        memory.firstBest("Thomas rode to Alabama", Language.EN, null)
      }
    }

  }


}
