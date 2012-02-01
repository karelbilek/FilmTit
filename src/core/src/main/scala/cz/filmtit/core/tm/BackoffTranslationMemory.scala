package cz.filmtit.core.tm

import cz.filmtit.core.model._
import cz.filmtit.core.model.Language._
import com.weiglewilczek.slf4s.{Logging}


/**
 * @author Joachim Daiber
 *
 */

class BackoffTranslationMemory(val storage: TranslationPairStorage,
                               val ranker: TranslationPairRanker,
                               val backoff: Option[TranslationMemory] = None,
                               val threshold: Double = 0.90
                                ) extends TranslationMemory with Logging {

  def candidates(chunk: Chunk, mediaSource: MediaSource, language: Language) = {

    val s = System.currentTimeMillis
    val candidates = storage.candidates(chunk, language)

    logger.info( "Retrieved %d candiates in %dms...".format(candidates.size,
      System.currentTimeMillis - s) )

    candidates
  }


  def nBest(chunk: Chunk, mediaSource: MediaSource, language: Language,
            n: Int = 10): List[ScoredTranslationPair] = {

    val s = System.currentTimeMillis

    val ranked = ranker.rank(chunk, null, candidates(chunk, mediaSource, language))
    logger.info( "Ranking candiates took %dms..."
      .format(System.currentTimeMillis - s) )

    if ( ranked.take(n).exists(pair => pair.finalScore >= threshold) )
      ranked.take(n)
    else
      backoff match {
        case Some(backoffTM) => backoffTM.nBest(chunk, mediaSource, language, n)
        case None => List[ScoredTranslationPair]()
      }
  }


  def firstBest(chunk: Chunk, mediaSource: MediaSource, language: Language):
    Option[ScoredTranslationPair] = {

    ranker.best(chunk, null, candidates(chunk, mediaSource, language)) match {
      case Some(best) if best.finalScore >= threshold => Some(best)
      case _ =>
        backoff match {
          case Some(backoffTM) => backoffTM.firstBest(chunk, mediaSource, language)
          case None => None
        }
    }

  }

}

