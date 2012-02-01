package cz.filmtit.core.rank

import cz.filmtit.core.model._

import cz.filmtit.core.model.Chunk._
import org.apache.commons.lang3.StringUtils

/**
 * @author Joachim Daiber
 *
 *
 *
 */

class ExactRanker extends TranslationPairRanker {

  val lambdas = (0.95, 0.05)

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair):
  ScoredTranslationPair = {
    val editDistanceScore = 1.0 - (
      StringUtils.getLevenshteinDistance(chunk,pair.source)
      / chunk.length.toFloat)

    val genreMatches = if (mediaSource != null) {
      pair.mediaSource.genres.intersect(mediaSource.genres).size / mediaSource.genres.size.toFloat
    } else {
      0.0
    }

    ScoredTranslationPair.fromTranslationPair(pair,
      ((lambdas._1 * editDistanceScore) + (lambdas._2 * genreMatches)))
  }

  override def name = "Exact Levensthein-based ranking."


}