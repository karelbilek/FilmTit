package cz.filmtit.core.tm

import _root_.java.io.IOException
import cz.filmtit.core.model.{TranslationPairMerger, TranslationPairSearcher, TranslationPairRanker, TranslationMemory}
import cz.filmtit.core.concurrency.tokenizer.TokenizerWrapper

import org.apache.commons.logging.LogFactory
import scala.Predef._
import cz.filmtit.core.model.storage.{MediaStorage, TranslationPairStorage}
import cz.filmtit.core.search.postgres.BaseStorage
import cz.filmtit.core.concurrency.searcher.TranslationPairSearcherWrapper
import cz.filmtit.share._
import collection.mutable.ListBuffer
import exceptions.{SearcherNotAvailableException, LanguageNotSupportedException}


/**
 * Simple [[cz.filmtit.core.model.TranslationMemory]] implementation with
 * a number of backoff TMs. Each TM can have a threshold score (over which
 * it outputs translation pairs) and a backoff TM, which is used if none of
 * the pairs are over the threshold.
 *
 * @author Joachim Daiber
 */

class BackoffTranslationMemory(
  val l1:Language,
  val l2:Language,
  val levels: List[BackoffLevel],
  val merger: Option[TranslationPairMerger],
  val tokenizerl1: Option[TokenizerWrapper] = None,
  val tokenizerl2: Option[TokenizerWrapper] = None
) extends TranslationMemory {

  val LOG = LogFactory.getLog("BackoffTM")

  override val mediaStorage = searchers.head match {
    case s: BaseStorage => s.asInstanceOf[MediaStorage]
    case _ => null
  }

  def tokenizer(language:Language) = language match {
        case `l1` => tokenizerl1
        case `l2` => tokenizerl2
        case _ => throw new Exception("Wrong tokenization language")
    }

  def nBest(chunk: Chunk, language: Language, mediaSource: MediaSource,
    n: Int = 10, inner: Boolean = false,
    forbiddenSources: java.util.Set[TranslationSource] = java.util.Collections.emptySet[TranslationSource]
  ): List[TranslationPair] = {


    if(!chunk.isActive) {
      List[TranslationPair]()
    }

    tokenize(chunk, language)
    LOG.info( "n-best: (%s) %s".format(language, chunk) )

    var results = ListBuffer[TranslationPair]()
    for (level: BackoffLevel <- this.levels.filter({ l: BackoffLevel => !forbiddenSources.contains(l.translationType) })) {

      val s1 = System.currentTimeMillis
      try {
        val pairs = level.searcher.candidates(chunk, language)
        val s2 = System.currentTimeMillis

        results ++= (level.ranker match {
          case Some(r) => r.rank(chunk, mediaSource, pairs)
          case None => pairs
        }).filter(pair => pair.getScore >= level.threshold)

        val s3 = System.currentTimeMillis

        LOG.info( level.toString + ": retrieved %d candidates (%dms), ranking: %dms, total: %dms, Chunk: %s"
          .format(pairs.size, s2 - s1, s3 - s2, s3 - s1, chunk) )

        if ( results.size >= n ) {
          return merge(results.sorted, n)
        }
      } catch {
        case e: LanguageNotSupportedException => //The searcher does not support the requested language, we cannot use it.
        case e: SearcherNotAvailableException => LOG.warn("Searcher %s not available.".format(level.searcher.getClass.getSimpleName))
      }
    }

    merge(results.sorted, n)
  }

  def firstBest(chunk: Chunk, language: Language, mediaSource: MediaSource,
                forbiddenSources: java.util.Set[TranslationSource] = java.util.Collections.emptySet[TranslationSource]):
  Option[TranslationPair] = nBest(chunk, language, mediaSource).headOption


  def merge(results: Seq[TranslationPair], n: Int): List[TranslationPair] = {
    merger match {
      case Some(m) => m.merge(results.toList, n)
      case None => results.toList.take(n)
    }
  }


  def add(pairs: Array[TranslationPair]) {

    LOG.info( "Tokenizing..." )
    pairs.foreach{ p => tokenizeForImport(p) }
    LOG.info( "Done." )

    searchers.head match {
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

  def finishImport() {
    LOG.info( "Finishing import..." )
    searchers.head match {
      case s: TranslationPairStorage => s.finishImport()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.finishImport()
          case _ =>
        }
      }
      case _ =>
    }
  }

  def warmup() {
    LOG.info("Warming up...")
    searchers.foreach(_ match {
       case s: TranslationPairStorage => s.warmup()
       case s: TranslationPairSearcherWrapper => {
         s.searchers.head match {
           case s: TranslationPairStorage => s.warmup()
           case _ =>
         }
       }
       case _ =>
      })
  }

  def reindex() {

    //If the searcher can be reindexed, do it:
    LOG.info("Reindexing...")
    searchers.foreach(_ match {
      case s: TranslationPairStorage => s.reindex()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.reindex()
          case _ =>
        }
      }
      case _ =>
    })

  }

  def reset() {
    LOG.info("Reseting...")
    searchers.foreach(_ match {
      case s: TranslationPairStorage => s.reset()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.reset()
          case _ =>
        }
      }
      case _ =>
    })
  }

  def close() {
    tokenizerl1.foreach(_.close())
    tokenizerl2.foreach(_.close())

    searchers.foreach(_ match {
      case s: TranslationPairStorage => s.close()
      case s: TranslationPairSearcherWrapper => {
        s.searchers.head match {
          case s: TranslationPairStorage => s.close()
          case _ =>
        }
      }
      case _ =>
    })
  }

  private def tokenize(chunk:Chunk, language:Language) {
    if (!chunk.isTokenized) {
       //foreach means "do if not None"
        tokenizer(language).foreach {_.tokenize(chunk)}
    }
  }

  private def tokenizeForImport(pair:TranslationPair) {
    pair.getChunkL1.setTokens(tokenizer(l1).get.tokenizers(0).tokenize(pair.getChunkL1.getSurfaceForm))
    pair.getChunkL2.setTokens(tokenizer(l2).get.tokenizers(0).tokenize(pair.getChunkL2.getSurfaceForm))
  }

  def searchers = levels.map(_.searcher)
}

