package cz.filmtit.core.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.core.concurrency.TranslationPairSearcherWrapper
import java.io.File
import cz.filmtit.core.{Factory, Configuration}
import cz.filmtit.core.search.postgres.impl.NEStorage
import cz.filmtit.share.{TranslationPair, Chunk, Language}

/**
 * @author Joachim Daiber
 *
 *
 *
 */

@RunWith(classOf[JUnitRunner])
class ConcurrentSearcherSpec extends Spec {

  describe("A concurrent searcher") {

    val configuration = new Configuration(new File("configuration.xml"))
    val recognizers = Factory.createNERecognizersFromConfiguration(configuration)
    val connection = Factory.createConnection(configuration)

    it("is created with a list of normal searchers (e.g. 4 searchers)") {
      val searcher = new TranslationPairSearcherWrapper(
        (0 to 4).map { _ => new NEStorage(Language.EN, Language.CS, connection, recognizers._1, recognizers._2 ) }.toList
      )

      val candidates: List[TranslationPair] = searcher.candidates(new Chunk("Peter"), Language.EN)
      searcher.candidates(new Chunk("Peter"), Language.EN)
    }

  }
}
