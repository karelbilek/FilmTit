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

import _root_.java.io.File
import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._
import cz.filmtit.core.Utils.min
import cz.filmtit.core.model.Patterns
import scala.Double

/**
 * Translation pair ranker for exact matches.
 *
 * @author Joachim Daiber
 */

trait ExactRanker extends BaseRanker {

  def getScoreNames = List("count", "edit_distance", "genres", "translation_length_penaltiy", "punctuation")

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
      punctuationMatches(chunk.getSurfaceForm, pair.getStringL2)
    )
  }

  override def name = "Exact Levensthein-based ranking."

}

class ExactWekaRanker(modelFile: File) extends WEKARanker(modelFile) with ExactRanker
class ExactLRRanker(weights: List[Double]) extends LinearRegressionRanker(weights: List[Double]) with ExactRanker