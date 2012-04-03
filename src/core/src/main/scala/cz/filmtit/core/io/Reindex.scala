package cz.filmtit.core.io

import cz.filmtit.core.Factory

/**
 * @author Joachim Daiber
 */

object Reindex {
  def main(args: Array[String]) {
    Factory.createTM(readOnly = false).reindex()
  }
}
