package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 *
 *
 */

class ScoredTranslationPair(sourceSentence: String, tagetSentence: String,  candidateScore: Float, finalScore: Float)
  extends TranslationPair(sourceSentence, targetSentece) with Ordered[ScoredTranslationPair] {

  override def compare (that: ScoredTranslationPair) = {
    (finalScore * 10 + candidateScore) -
    (that.finalScore * 10 + that.candidateScore)
  }

}