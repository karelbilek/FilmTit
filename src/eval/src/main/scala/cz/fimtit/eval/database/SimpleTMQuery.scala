package cz.fimtit.eval.database

import cz.filmtit.core.model.Language
import cz.filmtit.core.factory.Factory


/**
 * @author Joachim Daiber
 *
 *
 *
 */

object SimpleTMQuery {
  def main(args: Array[String]) {

    val tm = Factory.createTM()

    println(tm.nBest("I love you!", null, Language.en))
    println(tm.nBest("What did the minister tell you about his intentions?", null, Language.en))
    println(tm.nBest("Peter called.", null, Language.en))
    println(tm.nBest("Dear Mr. Bush", null, Language.en))


  }
}