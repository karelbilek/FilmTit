package cz.filmtit.core.tests

import scala.Array
import cz.filmtit.share.TranslationPair
import cz.filmtit.core.model.TranslationMemory
import cz.filmtit.core.{Configuration, Factory}

object TestUtil {

  def createTMWithDummyContent(configuration: Configuration): TranslationMemory = {
    val tm = Factory.createTMFromConfiguration(
      configuration,
      readOnly=false,
      useInMemoryDB=true
    )

    tm.reset()
    tm.add(Array(
      new TranslationPair("Peter rode to Alabama.", "Petr jel do Alabamy."),
      new TranslationPair("Peter rode to Alaska.", "Petr jel do Aljašky.")
    ))
    tm.reindex()
    tm
  }

}
