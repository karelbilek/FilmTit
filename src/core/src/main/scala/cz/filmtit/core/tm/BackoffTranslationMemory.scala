package cz.filmtit.core.tm

import com.weiglewilczek.slf4s.Logger
import cz.filmtit.core.model.data._
import cz.filmtit.core.model.{TranslationPairSearcher, TranslationPairRanker, TranslationMemory}

import cz.filmtit.core.model.Language
import scala.Predef._
import cz.filmtit.core.model.storage.{MediaStorage, TranslationPairStorage}
import cz.filmtit.core.search.postgres.BaseStorage


/**
 * Simple [[cz.filmtit.core.model.TranslationMemory]] implementation with
 * a number of backoff TMs. Each TM can have a threshold score (over which
 * it outputs translation pairs) and a backoff TM, which is used if none of
 * the pairs are over the threshold.
 *
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
  
  override val mediaStorage = searcher match {
    case s: BaseStorage => s.asInstanceOf[MediaStorage]
    case _ => null
  }

  def candidates(chunk: Chunk, language: Language, mediaSource: MediaSource) = {

    val s = System.currentTimeMillis
    val candidates = searcher.candidates(chunk, language)

    logger.info( "Retrieved %d candiates in %dms...".format(candidates.size,
      System.currentTimeMillis - s) )

    candidates
  }


  def nBest(chunk: Chunk, language: Language, mediaSource: MediaSource,
            n: Int = 10): List[ScoredTranslationPair] = {


    val s = System.currentTimeMillis

    val ranked = ranker.rank(chunk, null, candidates(chunk, language, mediaSource))

    logger.info( "n-best: (%s) %s".format(language, chunk) )
    logger.info( "Ranking candiates took %dms..."
      .format(System.currentTimeMillis - s) )

    if ( ranked.take(n).exists(pair => pair.score >= threshold) )
      ranked.take(n)
    else
      backoff match {
        case Some(backoffTM) => backoffTM.nBest(chunk, language, mediaSource, n)
        case None => List[ScoredTranslationPair]()
      }
  }


  def firstBest(chunk: Chunk, language: Language, mediaSource: MediaSource):
    Option[ScoredTranslationPair] = {

    logger.info( "first-best: (%s) %s".format(language, chunk) )
    ranker.best(chunk, null, candidates(chunk, language, mediaSource)) match {
      case Some(best) if best.score >= threshold => Some(best)
      case _ =>
        backoff match {
          case Some(backoffTM) => backoffTM.firstBest(chunk, language, mediaSource)
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

}

