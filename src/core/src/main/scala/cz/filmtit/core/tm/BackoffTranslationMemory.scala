package cz.filmtit.core.tm

import com.weiglewilczek.slf4s.Logger
import cz.filmtit.core.model.data._
import cz.filmtit.core.model.{TranslationPairSearcher, TranslationPairRanker, TranslationMemory}
import cz.filmtit.core.model.storage.TranslationPairStorage

import cz.filmtit.core.model.Language
import scala.Predef._


/**
 * @author Joachim Daiber
 *
 */

class BackoffTranslationMemory(
  val searcher: TranslationPairSearcher,
  val ranker: TranslationPairRanker,
  val backoff: Option[TranslationMemory] = None,
  val threshold: Double = 0.90
) extends TranslationMemory {

  val logger = Logger("BackoffTM[%s, %s]".format(
    searcher.getClass.getSimpleName,
    ranker.getClass.getSimpleName
  ))

  def candidates(chunk: Chunk, mediaSource: MediaSource, language: Language) = {

    val s = System.currentTimeMillis
    val candidates = searcher.candidates(chunk, language)

    logger.info( "Retrieved %d candiates in %dms...".format(candidates.size,
      System.currentTimeMillis - s) )

    candidates
  }


  def nBest(chunk: Chunk, mediaSource: MediaSource, language: Language,
            n: Int = 10): List[ScoredTranslationPair] = {

    logger.info( "n-best: (%s) %s".format(language, chunk) )

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

    logger.info( "first-best: (%s) %s".format(language, chunk) )
    ranker.best(chunk, null, candidates(chunk, mediaSource, language)) match {
      case Some(best) if best.finalScore >= threshold => Some(best)
      case _ =>
        backoff match {
          case Some(backoffTM) => backoffTM.firstBest(chunk, mediaSource, language)
          case None => None
        }
    }

  }

  def initialize(pairs: Array[TranslationPair]) {

    searcher match {
      case s: TranslationPairStorage => s.initialize(pairs)
      case _ =>
    }

    backoff match {
      case Some(tm) => tm.initialize(pairs)
      case None =>
    }
  }

  def reindex() {

    searcher match {
      case s: TranslationPairStorage => s.reindex()
      case _ =>
    }

    backoff match {
      case Some(tm) => tm.reindex()
      case None =>
    }
  }

  def addMediaSource(mediaSource: MediaSource): Long = {

    searcher match {
      case s: TranslationPairStorage => s.addMediaSource(mediaSource)
      case _ => throw new UnsupportedOperationException()
    }

  }

}

