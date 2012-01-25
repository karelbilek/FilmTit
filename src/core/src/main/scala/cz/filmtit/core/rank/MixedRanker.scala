package cz.filmtit.core.rank

import cz.filmtit.core.model._
import cz.filmtit.core.model.Chunk._
import org.apache.xmlbeans.impl.common.Levenshtein

/**
 * @author Joachim Daiber
 *
 *
 *
 */

class MixedRanker extends TranslationPairRanker {

  val lambdas = (0.95, 0.05)

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
    List[ScoredTranslationPair] = {
    pairs.map { pair =>
      val editDistanceScore = 1.0 - (Levenshtein.distance(chunk, pair.source) / chunk.length.toFloat)

      val genreMatches = if (mediaSource != null) {
        pair.mediaSource.genres.intersect(mediaSource.genres).size / mediaSource.genres.size.toFloat
      } else {
        0.0
      }

      ScoredTranslationPair.fromTranslationPair(pair, ((lambdas._1 * editDistanceScore) + (lambdas._2 * genreMatches)).toFloat)
    }.sorted
  }


  override def name = "Mixed methods for ranking"


}