package cz.filmtit.core.tm

import cz.filmtit.core.model.{TranslationPairSearcher, TranslationPairRanker, TranslationMemory}
import cz.filmtit.core.concurrency.tokenizer.TokenizerWrapper

import org.apache.commons.logging.LogFactory
import scala.Predef._
import cz.filmtit.core.model.storage.{MediaStorage, TranslationPairStorage}
import cz.filmtit.core.search.postgres.BaseStorage
import cz.filmtit.core.concurrency.searcher.TranslationPairSearcherWrapper
import cz.filmtit.share._


/**
 * Simple [[cz.filmtit.core.model.TranslationMemory]] implementation with
 * a number of backoff TMs. Each TM can have a threshold score (over which
 * it outputs translation pairs) and a backoff TM, which is used if none of
 * the pairs are over the threshold.
 *
 * @author Joachim Daiber
 */

class BackoffTranslationMemory(
  val searcher: TranslationPairSearcher,
  val l1:Language,
  val l2:Language,
  val ranker: Option[TranslationPairRanker] = None,
  val backoff: Option[TranslationMemory] = None,
  val threshold: Double = 0.90,
  val tokenizerl1: Option[TokenizerWrapper] = None,
  val tokenizerl2: Option[TokenizerWrapper] = None
  ) extends TranslationMemory {

  val logger = LogFactory.getLog("BackoffTM[%s, %s]".format(
    searcher match {
      case s: TranslationPairSearcherWrapper => { "%s (%d concurrent instances)".format(s.searchers.head.getClass.getSimpleName, s.size) }
      case s: TranslationPairSearcher => s.getClass.getSimpleName
    },
    ranker match {
      case Some(r) => r.getClass.getSimpleName
      case None => "no ranker"
    }
  ))

  logger.info("Backoff TM created.")

  /**
   * TODO: this is a bit confusing
   *
   * If the searcher extends BaseStorage, then it will also be a MediaStorage,
   * hence the media storage for this TM will be the searcher
   */
  override val mediaStorage = searcher match {
    case s: BaseStorage => s.asInstanceOf[MediaStorage]
    case _ => null
  }
  
  def tokenizer(language:Language) = language match {
        case `l1` => tokenizerl1
        case `l2` => tokenizerl2
        case _ => throw new Exception("Wrong tokenization language")
    }


  def tokenize(pair:TranslationPair) {
    tokenize(pair.getChunkL1, l1)
    tokenize(pair.getChunkL2, l2)
  }

  def tokenize(chunk:Chunk, language:Language) {
    if (!chunk.isTokenized) {
       //foreach means "do if not None"
        tokenizer(language).foreach {_.tokenize(chunk)}
    }
  }

  def tokenizeForImport(pair:TranslationPair) {
    tokenizeForImport(pair.getChunkL1, l1)
    tokenizeForImport(pair.getChunkL2, l2)
  }

  def tokenizeForImport(chunk:Chunk, language:Language) {
    chunk.setTokens(tokenizer(language).get.tokenizers(0).tokenize(chunk.getSurfaceForm))
  }

  def candidates(chunk: Chunk, language: Language, mediaSource: MediaSource)={
    tokenize(chunk, language);
    searcher.candidates(chunk, language)
  }


  def nBest(chunk: Chunk, language: Language, mediaSource: MediaSource,
    n: Int = 10, inner: Boolean = false): List[TranslationPair] = {

    tokenize(chunk, language);
    //Only on first level:
    if (!inner)
      logger.info( "n-best: (%s) %s".format(language, chunk) )

    val s1 = System.currentTimeMillis
    val pairs: List[TranslationPair] = candidates(chunk, language, mediaSource)
    val s2 = System.currentTimeMillis

    val ranked = ranker match {
      case Some(r) => r.rank(chunk, null, pairs)
      case None => pairs
    }
    val s3 = System.currentTimeMillis

    logger.info( "Retrieved %d candidates (%dms), ranking: %dms, total: %dms, Chunk: %s"
      .format(pairs.size, s2 - s1, s3 - s2, s3 - s1, chunk) )

    if ( ranked.take(n).exists(pair => pair.getScore >= threshold) )
      ranked.take(n)
    else
      backoff match {
        case Some(backoffTM) => 
          backoffTM.nBest(chunk, language, mediaSource, n, inner=true)
        case None => List[TranslationPair]()
      }
  }


  def firstBest(chunk: Chunk, language: Language, mediaSource: MediaSource):
  Option[TranslationPair] = {

    tokenize(chunk, language);
    logger.info( "first-best: (%s) %s".format(language, chunk) )

    val pairs: List[TranslationPair] = candidates(chunk, language, mediaSource)
    val best = ranker match {
      case Some(r) => r.best(chunk, null, pairs)
      case None => pairs.headOption
    }

    best match {
      case Some(pair) if pair.getScore >= threshold => Some(pair)
      case _ =>
        backoff match {
          case Some(backoffTM) => backoffTM.firstBest(chunk, language, mediaSource)
          case None => None
        }
    }

  }

  def add(pairs: Array[TranslationPair]) {

    logger.info( "Tokenizing..." )
    pairs.foreach{ p => tokenizeForImport(p) }
    logger.info( "Done." )

    //If the searcher can be initialized with translation pairs, do it:
    searcher match {
      case s: TranslationPairStorage => s.add(pairs)
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.add(pairs)
          case _ =>
        }
      }
      case _ =>
    }

  }

  def warmup() {

    logger.info("Warming up...")

     //If the searcher can be warmed up, do it:
     searcher match {
       case s: TranslationPairStorage => s.warmup()
       case s: TranslationPairSearcherWrapper => {
         s.searchers.head match {
           case s: TranslationPairStorage => s.warmup()
           case _ =>
         }
       }
       case _ =>
      }

      if (backoff.isDefined) backoff.get.warmup()
  }

  def reindex() {

    //If the searcher can be reindexed, do it:
    searcher match {
      case s: TranslationPairStorage => s.reindex()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.reindex()
          case _ =>
        }
      }
      case _ =>
    }

    //If there is a backoff TM (there is either 0 or 1 backoff TM), reindex it
    if (backoff.isDefined) backoff.get.reindex()

  }

  def reset() {
    //If the searcher can be reset, do it:
    searcher match {
      case s: TranslationPairStorage => s.reset()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.reset()
          case _ =>
        }
      }
      case _ =>
    }

  }

  def close() {
    tokenizerl1.foreach(_.close())
    tokenizerl2.foreach(_.close())

    searcher match {
      case s: TranslationPairStorage => s.close()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.close()
          case _ =>
        }
      }
      case _ =>
    }
  }
}

