package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.core.search.external.MyMemorySearcher
import cz.filmtit.share.Language
import cz.filmtit.core.Utils.chunkFromString

/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class SearcherSpec extends Spec {

  val searchers = List(
    new MyMemorySearcher(Language.EN, Language.CS)
  )

  describe("A Searcher") {
    it("should return a translation for a sentence") {
      searchers.foreach(s =>
        assert(s.candidates("My name is George Bush.", Language.EN).size > 0)
      )
    }
  }


}
