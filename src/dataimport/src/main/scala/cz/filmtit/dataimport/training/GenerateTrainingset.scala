package cz.filmtit.dataimport.training

import _root_.java.io.{FileOutputStream, PrintWriter, File}
import cz.filmtit.userspace.{USTranslationResult, FilmTitBackendServer}
import cz.filmtit.core.{Configuration, ConfigurationSingleton}
import cz.filmtit.share.{TranslationPair, TranslationResult}
import scala.collection.JavaConversions._
import cz.filmtit.core.rank.ExactRanker


/**
 * @author Joachim Daiber
 *
 */

object GenerateTrainingset {

  def main(args: Array[String]) {
    ConfigurationSingleton.setConf(new Configuration(new File("configuration.xml")))
    //val backend = new FilmTitBackendServer()

    val results: java.util.List[TranslationResult] = USTranslationResult.getUncheckedResults
    val exactOutputWriter = new PrintWriter(new FileOutputStream("exact_weka.csv"))

    println("Got %d results".format(results.size))

    val ranker = new ExactRanker()

    (0 to results.size - 1).foreach {
      i: Int =>
        val result = results.get(i)
        var klass = 0

        val firstPair = result.getSelectedTranslationPairID match {
          case tpID: Long if tpID != 0 => {
            //Use the selected translation as a positive example
            klass = 1
            result.getTmSuggestions.get(tpID.toInt)
          }
          case _ if result.getTmSuggestions.size() > 0 => {
            //Use the first suggestion as a negative example
            klass = 0
            result.getTmSuggestions.get(0)
          }
          case _ => null
          //case _ => new TranslationPair(result.getSourceChunk.getSurfaceForm, result.getUserTranslation)
        }

        if (firstPair != null && !(firstPair.getStringL2 equals "")) {
          val totalCount = result.getTmSuggestions.map(_.getCount).sum
          val scores = ranker.getScores(result.getSourceChunk, null, firstPair, totalCount)
          exactOutputWriter.print((scores :: List(klass)).mkString("\"", "\", \"", "\""))
        }
    }
    exactOutputWriter.close()

  }

}