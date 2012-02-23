package cz.filmtit.core.rank

import cz.filmtit.core.model.data.{TranslationPair, MediaSource}
import cz.filmtit.core.model.TranslationPairRanker


/**
 * @author Joachim Daiber
 *
 */

abstract class BaseRanker extends TranslationPairRanker {

  def genreMatches(mediaSource: MediaSource, pair: TranslationPair): Double =
    if (mediaSource != null) {
      pair.mediaSource.genres.intersect(mediaSource.genres).size / mediaSource.genres.size.toFloat
    } else {
      0.0
    }

}
