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
    val tm: TranslationMemory = Factory.createTMFromConfiguration(configuration, readOnly = false)
    tm.reindex()
    tm.close()
  }
}
