package cz.filmtit.dataimport.training

import _root_.java.io.{FileOutputStream, PrintWriter, File}
//import cz.filmtit.userspace.{USTranslationResult, FilmTitBackendServer}
import cz.filmtit.userspace.USTranslationResult
import cz.filmtit.share.{TranslationPair, TranslationResult}
import scala.collection.JavaConversions._
import cz.filmtit.core.rank.ExactRanker
import cz.filmtit.core.{Factory, Configuration, ConfigurationSingleton}
import cz.filmtit.share.TranslationPair._


/**
 * @author Joachim Daiber
 *
 */

object GenerateTrainingset {

  def main(args: Array[String]) {
    ConfigurationSingleton.setConf(new Configuration(new File("configuration.xml")))
    //val backend = new FilmTitBackendServer()

    val TM = Factory.createTMFromConfiguration(
            ConfigurationSingleton.getConf(),
            true, // readonly
            false  // in memory
    );

    val results: java.util.List[USTranslationResult] = USTranslationResult.getUncheckedResults
    val exactOutputWriter = new PrintWriter(new FileOutputStream("exact_weka.csv"))

    println("Got %d results".format(results.size))

    val ranker = new ExactRanker()

    exactOutputWriter.println("\"" + ranker.getScoreNames.mkString("\",\"") + "\"")
    (0 to results.size - 1).foreach {
      i: Int =>
        val result = results.get(i)
        var klass = 0
        result.generateMTSuggestions(TM)

        val firstPair = result.getSelectedTranslationPairID match {
          case tpID: Long if tpID != 0 => {
            //Use the selected translation as a positive example
            klass = 1
            try {
              result.getTranslationResult.getTmSuggestions.filter(_.getId == tpID.toInt).head
            } catch {
              case e: NoSuchElementException => null
            }
          }
          case _ if result.getTranslationResult.getTmSuggestions.size() > 0 => {
            //Use the first suggestion as a negative example
            klass = 0
            result.getTranslationResult.getTmSuggestions.get(0)
          }
          case _ => null
          //case _ => new TranslationPair(result.getSourceChunk.getSurfaceForm, result.getUserTranslation)
        }

        if (firstPair != null && !(firstPair.getStringL2 equals "")) {
          val totalCount = result.getTranslationResult.getTmSuggestions.map(_.getCount).sum
          val scores = ranker.getScores(result.getTranslationResult.getSourceChunk, null, firstPair, totalCount)
          exactOutputWriter.println("\"" + (scores ::: List(klass)).mkString("\",\"") + "\"")
        }
    }
    exactOutputWriter.close()
    TM.close()
  }

}
