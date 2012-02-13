package cz.filmtit.core.model.data

import cz.filmtit.core.model.TranslationSource

/**
 * @author Joachim Daiber
 *
 *
 */
class ScoredTranslationPair(
  chunkL1: Chunk,
  chunkL2: Chunk,
  source: TranslationSource,
  mediaSource: MediaSource,
  var score: Double = 0.0,
  var priorScore: Double = 0.0
) extends TranslationPair(chunkL1, chunkL2, source, mediaSource) with
  Ordered[ScoredTranslationPair] {

  override def compare(that: ScoredTranslationPair): Int = {
    math.signum(
      (that.score * 10 + that.priorScore) - (this.score * 10 + this.priorScore)
    ).toInt
  }

  override def toString = "%s[score: %.2f, prior: %.2f]".format(super
    .toString, score,
    priorScore)

}

object ScoredTranslationPair {

  implicit def fromTranslationPair(pair: TranslationPair): ScoredTranslationPair = {
    new ScoredTranslationPair(pair.chunkL1, pair.chunkL2,
      pair.source, pair.mediaSource)
  }

  def fromTranslationPair(pair: TranslationPair, score: Double): ScoredTranslationPair = {
    new ScoredTranslationPair(pair.chunkL1, pair.chunkL2,
      pair.source, pair.mediaSource, score)
  }

}
