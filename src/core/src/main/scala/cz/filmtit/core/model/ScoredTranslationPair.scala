package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 *
 *
 */

class ScoredTranslationPair(sourceSentence: String, targetSentence: String,  val candidateScore: Float, val finalScore: Float)
  extends TranslationPair(sourceSentence, targetSentence) with Ordered[ScoredTranslationPair] {

  override def compare (that: ScoredTranslationPair): Int = {
    math.signum(
      (finalScore * 10 + candidateScore) -
      (that.finalScore * 10 + that.candidateScore)
    ).toInt
  }

}