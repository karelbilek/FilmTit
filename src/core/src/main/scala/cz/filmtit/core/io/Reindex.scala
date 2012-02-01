package cz.filmtit.core.io

import cz.filmtit.core.factory.TMFactory

/**
 * @author Joachim Daiber
 */

object Reindex {
  def main(args: Array[String]) {
    TMFactory.defaultTM().reindex()
  }
}
