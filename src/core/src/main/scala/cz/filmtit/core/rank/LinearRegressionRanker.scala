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