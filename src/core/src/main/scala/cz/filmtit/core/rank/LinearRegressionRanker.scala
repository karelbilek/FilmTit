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

import cz.filmtit.share.{TranslationPair, MediaSource, Chunk}

/**
 * A translation pair ranker based on Linear Regression. The parameters (weights) of this
 * model can be estimated using the WEKA machine learning toolkit and it is not necessary
 * to add WEKA as a dependency for this ranker.
 *
 * @author Joachim Daiber
 */

abstract class LinearRegressionRanker(val weights: List[Double]) extends BaseRanker {

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]): List[TranslationPair] = {

    val totalCount = pairs.map(_.getCount).sum
    pairs.foreach{ pair: TranslationPair =>
      pair.setScore( weightedSum(getScores(chunk, mediaSource, pair, totalCount)) )
    }

    pairs.sorted
  }

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = pair

  def weightedSum(scores: List[Double]): Double = {
    weights.zip(scores ::: List[Double](1.0)).map{ case(w, s) => w*s }.sum
  }

}