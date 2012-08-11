package cz.filmtit.dataimport.training

import _root_.java.io.{FileOutputStream, PrintWriter, File}
import scala.util.Random

//import cz.filmtit.userspace.{USTranslationResult, FilmTitBackendServer}
import cz.filmtit.userspace.USTranslationResult
import cz.filmtit.share.{TranslationPair, TranslationResult}
import scala.collection.JavaConversions._
import cz.filmtit.core.rank.{FuzzyLRRanker, ExactLRRanker, FuzzyRanker, ExactRanker}
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
            readOnly = true, // readonly
            useInMemoryDB = false  // in memory
    );

    val results: java.util.List[USTranslationResult] = USTranslationResult.getUncheckedResults
    val exactOutputWriter = new PrintWriter(new FileOutputStream("fuzzy_weka.csv"))

    println("Got %d results".format(results.size))

    //val ranker = new FuzzyRanker(ConfigurationSingleton.getConf().fuzzyRankerWeights)
    val ranker = new FuzzyLRRanker(ConfigurationSingleton.getConf().fuzzyRankerWeights)

    exactOutputWriter.println("\"" + (ranker.getScoreNames ::: List("class")).mkString("\",\"") + "\"")
    (0 to results.size - 1).foreach {
      i: Int =>
        val result = results.get(i)
        var klass = "false"
        result.generateMTSuggestions(TM)

        val firstPair = result.getSelectedTranslationPairID match {
          case tpID: Long if tpID != 0 => {
            //Use the selected translation as a positive example
            klass = "true"
            try {
              result.getTranslationResult.getTmSuggestions.filter(_.getId == tpID.toInt).head
            } catch {
              case e: NoSuchElementException => null
            }
          }
          case _ if result.getTranslationResult.getTmSuggestions.size() > 0 => {
            //Use the first suggestion as a negative example
            klass = "false"
            //We need to choose a random negative example, since the examples are ordered by their count.
            val i = Random.nextInt(math.min(10, result.getTranslationResult.getTmSuggestions.size()))

            result.getTranslationResult.getTmSuggestions.get(i)
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
