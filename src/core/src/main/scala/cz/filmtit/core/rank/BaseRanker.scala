/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.core.rank

import cz.filmtit.core.model.{Patterns, TranslationPairRanker}
import scala.collection.JavaConversions._
import cz.filmtit.share.{Chunk, TranslationPair, MediaSource}


/**
 * Base class for TranslationPairRanker implementations.
 *
 * @author Joachim Daiber
 */

abstract class BaseRanker extends TranslationPairRanker {

  /**
   * Feature indicating the overlap of genres between the media source of a queried [[cz.filmtit.share.Chunk]]
   * and a translation pair candidate.
   *
   * @param mediaSource the media source of the queried chunk
   * @param pair the translation pair candidate
   * @return
   */
  def genreMatches(mediaSource: MediaSource, pair: TranslationPair): Double =
    if (mediaSource != null) {
      pair.getMediaSources.toList.flatMap(_.getGenres).toSet.intersect(mediaSource.getGenres).size / mediaSource.getGenres.size.toFloat
    } else {
      0.0
    }

  /**
   * Feature indicating whether the chunk-final punctuation of a source and target chunk matches.
   *
   * @param source the source chunk
   * @param translation the target chunk
   * @return 1.0 if the punctuation matches, 0.0 if not
   */
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

  /**
   * Return the names/labels of the scores used in the ranker. This method is used when creating input files
   * for machine learning libraries.
   *
   * @return the list of labels for the scores in the same order as the scores
   */
  def getScoreNames: List[String]

  /**
   * Calculate all scores/features for the chunk, media source and translation pair.
   *
   * @param chunk the chunk that is queried
   * @param mediaSource the media source of the queried chunk
   * @param pair
   * @param totalCount
   * @return
   */
  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double]

}
