package cz.fimtit.eval.database

import cz.filmtit.core.model.Language
import cz.filmtit.core.factory.TMFactory
import cz.filmtit.core.TMFactory

/**
 * @author Joachim Daiber
 *
 *
 *
 */

object SimpleTMQuery {
  def main(args: Array[String]) {

    val tm = TMFactory.defaultTM()

    println(tm.nBest("I love you!", null, Language.en))
    println(tm.nBest("What did the minister tell you about his intentions?", null, Language.en))
    println(tm.nBest("Never gonna give you up!", null, Language.en))
    println(tm.nBest("What?", null, Language.en))


  }
}