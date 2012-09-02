package cz.filmtit.core.tests

import java.util
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


import org.scalatest.Spec
import cz.filmtit.share.MediaSource
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory


/**
 * Test specification for the data model classes.
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class MediaSourceFactorySpec extends Spec {

  val msFactory = new FreebaseMediaSourceFactory()

  describe("A MediaSourceFactory") {

    it("should find entries for very popular movies") {
      assert(msFactory.getSuggestions("Matrix").size() > 0)
    }

    it("should find the best entry for a very popular movie") {
      assert(msFactory.getSuggestion("Matrix", "1999").getTitle.equals("The Matrix"))
    }

    it("should find entries for not so popular movies") {
      val jaws: util.List[MediaSource] = msFactory.getSuggestions("Jaws")
      assert(jaws.size() > 0)
    }

  }
}
