package cz.filmtit.core.rank

import cz.filmtit.core.model.{Patterns, TranslationPairRanker}
import scala.collection.JavaConversions._
import cz.filmtit.share.{Chunk, TranslationPair, MediaSource}


/**
 * @author Joachim Daiber
 *
 */

abstract class BaseRanker extends TranslationPairRanker {

  def genreMatches(mediaSource: MediaSource, pair: TranslationPair): Double =
    if (mediaSource != null) {
      pair.getMediaSources.toList.flatMap(_.getGenres).toSet.intersect(mediaSource.getGenres).size / mediaSource.getGenres.size.toFloat
    } else {
      0.0
    }

  def punctuationMatches(source: String, translation: String): Double = {
    translation match {
      case Patterns.finalpunctuation(p) => {
        if (source.endsWith(p))
          return 1.0
        else
          return 0.0
      }
      case _ => 1.0
    }
  }

  def getScoreNames: List[String]
  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double]

}
