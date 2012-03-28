package cz.fimtit.eval.database

import cz.filmtit.core.Factory
import java.net.ConnectException
import cz.filmtit.core.model.{TranslationMemory, Language}


/**
 * @author Joachim Daiber
 *
 *
 *
 */

object SimpleTMQuery {
  def main(args: Array[String]) {

    println("Starting translation memory...")
    
    val tm: TranslationMemory = try {
      Factory.createTM()
    } catch {
      case e: ConnectException => {
        println("Error: " + e.getMessage)
        System.exit(1)
        null
      }
    }
    println("Done.")

    
    
    println(tm.nBest("I love you!", Language.en, null))
    println(tm.nBest("What did the minister tell you about his intentions?",
      Language.en, null))
    println(tm.nBest("Call the police, Peter", Language.en, null))
    println(tm.nBest("Peter opened the door.", Language.en, null))
    println(tm.nBest("Watch out!", Language.en, null))


  }
}