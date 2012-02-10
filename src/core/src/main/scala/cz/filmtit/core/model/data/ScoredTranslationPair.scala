package cz.filmtit.core.model.data

/**
 * @author Joachim Daiber
 *
 *
 */
class ScoredTranslationPair(source: Chunk, target: Chunk,
                            mediaSource: MediaSource, var finalScore: Double = 0.0)
  extends TranslationPair(source, target, mediaSource) with Ordered[ScoredTranslationPair] {

  var candidateScore = 0.0

  override def compare(that: ScoredTranslationPair): Int = {
    math.signum(
      (that.finalScore * 10 + that.candidateScore) - (this.finalScore * 10 + this.candidateScore)
    ).toInt
  }

  override def toString: String = "%s @ %.2f".format(super.toString, finalScore)


}

object ScoredTranslationPair {

  implicit def fromTranslationPair(pair: TranslationPair): ScoredTranslationPair = {
    new ScoredTranslationPair(pair.source, pair.target, pair.mediaSource)
  }

  def fromTranslationPair(pair: TranslationPair, score: Double): ScoredTranslationPair = {
    new ScoredTranslationPair(pair.source, pair.target, pair.mediaSource, score)
  }

}
