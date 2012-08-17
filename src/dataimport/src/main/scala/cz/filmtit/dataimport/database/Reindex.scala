package cz.filmtit.dataimport.database

import cz.filmtit.core.{Configuration, Factory}
import java.io.File
import cz.filmtit.core.model.TranslationMemory


/**
 * @author Joachim Daiber
 */

object Reindex {
  def main(args: Array[String]) {
    val configuration = new Configuration(new File(args(0)))

    val tm: TranslationMemory = Factory.createTM(
          configuration.l1, configuration.l2,
          Factory.createConnection(configuration, readOnly=false),
          configuration,
          1,
          configuration.searcherTimeout,
          indexing=true
    )

    tm.reindex()
    tm.close()
  }
}
