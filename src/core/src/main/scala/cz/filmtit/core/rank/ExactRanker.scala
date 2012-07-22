package cz.filmtit.core.rank

import cz.filmtit.core.model.data._
import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._
import cz.filmtit.core.model.Patterns
import collection.mutable.ListBuffer

/**
 * @author Joachim Daiber
 *
 *
 */

class ExactRanker(val weights: List[Double] = List(1.0, 1.0, 0.2, 0.2, 0.2, 0.0)) extends BaseRanker {

  val MIN_EDIT_DISTANCE = 1

  def mergeSimilarResults(pairs: List[TranslationPair]): List[TranslationPair] = {

    var lastTargetString = ""
    pairs.map{ pair: TranslationPair =>
      val targetString = pair.getChunkL2.getSurfaceForm

      if (StringUtils.getLevenshteinDistance(lastTargetString, targetString) <= MIN_EDIT_DISTANCE) {
        null
      } else {
        lastTargetString = targetString
        pair
      }
    }.filter(_ != null)
  }

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]): List[TranslationPair] = {

    val totalCount = pairs.map(_.getCount).sum
    pairs.foreach{ pair: TranslationPair =>
      pair.setScore( getWeightedScore(getScores(chunk, mediaSource, pair, totalCount)) )
    }

    mergeSimilarResults(pairs.sorted)
  }

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = pair

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
        pair.getStringL1.length / pair.getStringL2.length
      else
        pair.getStringL2.length / pair.getStringL1.length
      ,

      //Does final punctuation match?
      pair.getStringL2.last match{
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
