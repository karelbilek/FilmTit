package cz.filmtit.core.model

import cz.filmtit.core.model.Chunk._

/**
 * @author Joachim Daiber
 *
 *
 */
class ScoredTranslationPair(source: String, target: String, mediaSource: MediaSource)
  extends TranslationPair(source, target, mediaSource) with Ordered[ScoredTranslationPair] {

  var candidateScore = 0.0
  var finalScore = 0.0

  override def compare (that: ScoredTranslationPair): Int = {
    math.signum(
      (finalScore * 10 + candidateScore) -
        (that.finalScore * 10 + that.candidateScore)
    ).toInt
  }

}

object ScoredTranslationPair {

  implicit def fromTranslationPair(pair: TranslationPair): ScoredTranslationPair = {
    new ScoredTranslationPair(pair.source, pair.target, pair.mediaSource)
  }

  def fromTranslationPair(pair: TranslationPair, score: Float): ScoredTranslationPair = {
    val scoredPair: ScoredTranslationPair = new ScoredTranslationPair(pair.source, pair.target, pair.mediaSource)
    scoredPair.finalScore = score
    scoredPair
  }

}
