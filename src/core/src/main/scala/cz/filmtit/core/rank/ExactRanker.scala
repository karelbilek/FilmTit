package cz.filmtit.core.rank

import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._
import cz.filmtit.core.Utils.min
import cz.filmtit.core.model.Patterns
import scala.Double

/**
 * @author Joachim Daiber
 *
 *
 */

class ExactRanker(val weights: List[Double] = List(0.091, 0.8441, 0.02163, 0.02163, 0.02163, 0.0)) extends BaseRanker {

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]): List[TranslationPair] = {

    val totalCount = pairs.map(_.getCount).sum
    pairs.foreach{ pair: TranslationPair =>
      pair.setScore( getWeightedScore(getScores(chunk, mediaSource, pair, totalCount)) )
    }

    pairs.sorted
  }

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = pair

  def getScoreNames = List("count","leven","genres","lengthpenalty","punctuation","class")

  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double] = {

    List(
      //TranslationPair support
      (pair.getCount / totalCount.toDouble),

      //Levenshtein:
      1.0 - math.min(StringUtils.getLevenshteinDistance(chunk.getSurfaceForm, pair.getChunkL1.getSurfaceForm) / chunk.getSurfaceForm.length.toFloat, 1.0),

      //Genre matches:
      genreMatches(mediaSource, pair),

      //Length difference between source and translation
      if (pair.getStringL1.length < pair.getStringL2.length)
        pair.getStringL1.length / pair.getStringL2.length.toDouble
      else
        pair.getStringL2.length / pair.getStringL1.length.toDouble
      ,

      //Does final punctuation match?
      pair.getStringL2.last match {
        case Patterns.punctuation() if pair.getStringL2.last != chunk.getSurfaceForm.last => 0.0
        case _ => 1.0
      }
    )
  }

  def getWeightedScore(scores: List[Double]): Double = {
    weights.zip(scores ::: List[Double](1.0)).map{ case(w, s) => w*s }.sum
  }

  override def name = "Exact Levensthein-based ranking."


}
