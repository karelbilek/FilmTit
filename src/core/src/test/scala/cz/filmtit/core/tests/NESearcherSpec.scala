package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.core.model.Language
import cz.filmtit.core.search.postgres.impl.NEStorage
import cz.filmtit.core.model.data.TranslationPair


/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class NESearcherSpec extends Spec {

  val searcher = new NEStorage(Language.en, Language.cs)

  describe("A NE searcher") {
    it("should be able to restore the NE in the chunk") {

      val candidates = searcher.candidates("Peter", Language.en)

      /* Since we found the results via NE matches, the corresponding NE
         annotations must be restorable from the database. */
      assert(
        candidates.exists({ _.chunkL1.toAnnotatedString() contains "<Person>"})
      )
    }
  }


}
