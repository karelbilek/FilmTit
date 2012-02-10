package cz.fimtit.eval.database

import cz.filmtit.core.model.Language
import cz.filmtit.core.Factory


/**
 * @author Joachim Daiber
 *
 *
 *
 */

object SimpleTMQuery {
  def main(args: Array[String]) {

    val tm = Factory.createTM()

    println(tm.nBest("I love you!", Language.en, null))
    println(tm.nBest("What did the minister tell you about his intentions?",
      Language.en, null))
    println(tm.nBest("Peter called.", Language.en, null))
    println(tm.nBest("Dear Mr. Bush", Language.en, null))


  }
}