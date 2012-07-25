package cz.filmtit.core.rank

import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._
import cz.filmtit.core.model.Patterns
import scala.Double

/**
 * @author Joachim Daiber
 *
 *
 */

class ExactRanker(val weights: List[Double] = List(0.091, 0.8441, 0.02163, 0.02163, 0.02163, 0.0)) extends BaseRanker {

  val MIN_EDIT_DISTANCE = 1

  def min(nums: Int*): Int = nums.min

  def levenshteinSmallerN(str1: String, str2: String, minDistance: Int): Boolean = {

    val lenStr1 = str1.length
    val lenStr2 = str2.length

    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)

    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j

    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j-1)) 0 else 1

      d(i)(j) = min(
        d(i-1)(j  ) + 1,     // deletion
        d(i  )(j-1) + 1,     // insertion
        d(i-1)(j-1) + cost   // substitution
      )

      if (i == j && d(i)(j) > MIN_EDIT_DISTANCE) {
        return false
      }

    }

    d(lenStr1)(lenStr2) <= MIN_EDIT_DISTANCE
  }

  def mergeSimilarResults(pairs: List[TranslationPair]): List[TranslationPair] = {

    var pairsToBeRemoved = Set[Int]()

    for (i <- (0 to pairs.size-1)) {
      if (!pairsToBeRemoved.contains(i)) {
        for (j <- (i+1 to pairs.size-1)) {
          if (levenshteinSmallerN(pairs(i).getStringL2, pairs(j).getStringL2, MIN_EDIT_DISTANCE)) {
            pairsToBeRemoved += j
          }
        }
      }
    }

    var mergedPairs = List[TranslationPair]()
    var i = 0
    for (pair <- pairs) {
      if (!pairsToBeRemoved.contains(i))
        mergedPairs :+= pair
      i += 1
    }

    mergedPairs
  }

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]): List[TranslationPair] = {

    val totalCount = pairs.map(_.getCount).sum
    pairs.foreach{ pair: TranslationPair =>
      pair.setScore( getWeightedScore(getScores(chunk, mediaSource, pair, totalCount)) )
    }

    mergeSimilarResults(pairs.sorted)
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
