package cz.filmtit.core.io

import cz.filmtit.core.{Configuration, Factory}
import java.io.File


/**
 * @author Joachim Daiber
 */

object Reindex {
  def main(args: Array[String]) {
    val configuration = new Configuration(new File(args(0)))
    Factory.createTMFromConfiguration(configuration, readOnly = false).reindex()
  }
}
