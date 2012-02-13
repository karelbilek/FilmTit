package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


import org.scalatest.Spec
import cz.filmtit.core.model.Language
import cz.filmtit.core.search.mt.MyMemorySearcher

/**
 * @author Joachim Daiber
 *
 */


@RunWith(classOf[JUnitRunner])
class SearcherSpec extends Spec {

  val searchers = List(
    new MyMemorySearcher(Language.en, Language.cs)
  )

  describe("A Searcher") {
    it("should return a translation for a sentence") {
      searchers.foreach(s =>
        assert(s.candidates("My name is George Bush.", Language.en).size > 0)
      )
    }
  }


}
