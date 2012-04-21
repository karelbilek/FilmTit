package cz.filmtit.core.rank

import cz.filmtit.core.model.TranslationPairRanker
import scala.collection.JavaConversions._
import cz.filmtit.share.{TranslationPair, MediaSource}


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

}
