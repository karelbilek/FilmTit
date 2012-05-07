package cz.filmtit.eval.database

import java.net.ConnectException
import cz.filmtit.core.model.TranslationMemory
import cz.filmtit.share.Language
import cz.filmtit.core.Utils.chunkFromString
import cz.filmtit.core.{Configuration, Factory}
import java.io.File


/**
 * @author Joachim Daiber
 *
 */

object SimpleTMQuery {
  def main(args: Array[String]) {

    println("Starting translation memory...")

    val configuration = new Configuration(new File("configuration.xml"))

    val tm: TranslationMemory = try {
      Factory.createTM(configuration)
    } catch {
      case e: ConnectException => {
        println("Error: " + e.getMessage)
        System.exit(1)
        null
      }
    }
    println("Done.")

    
    println(tm.nBest("How many?", Language.EN, null))
    println(tm.nBest("I love you!", Language.EN, null))
    println(tm.nBest("What did the minister tell you about his intentions?",
      Language.EN, null))
    println(tm.nBest("Call the police, Peter", Language.EN, null))
    println(tm.nBest("Peter opened the door.", Language.EN, null))


  }
}
