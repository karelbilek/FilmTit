package cz.fimtit.eval.database

import cz.filmtit.core.database.PostgresFirstLetterStorage
import cz.filmtit.core.rank.MixedRanker
import cz.filmtit.core.model.{TranslationMemory, Language}

/**
 * @author Joachim Daiber
 *
 *
 *
 */

object SimpleTMQuery {
  def main(args: Array[String]) {

    val storage = new PostgresFirstLetterStorage()
    val ranker = new MixedRanker()
    storage.l1 = Language.en
    storage.l2 = Language.cz
    val tm = new TranslationMemory(storage, ranker)


    println(tm.nBest("I love you!", null, Language.en))
    println(tm.nBest("What did the minister tell you about his intentions?", null, Language.en))
    println(tm.nBest("Never gonna give you up!", null, Language.en))
    println(tm.nBest("What?", null, Language.en))


  }
}