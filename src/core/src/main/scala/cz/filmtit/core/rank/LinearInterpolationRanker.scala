package cz.filmtit.core.rank

import cz.filmtit.share.{TranslationPair, MediaSource, Chunk}

/**
 * @author Joachim Daiber
 */

abstract class LinearInterpolationRanker(val weights: List[Double]) extends BaseRanker {

  def getScoreNames: List[String]
  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double]

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