package cz.filmtit.core.tm

import cz.filmtit.core.model.{TranslationPairRanker, TranslationPairSearcher}
import cz.filmtit.core.concurrency.searcher.TranslationPairSearcherWrapper
import cz.filmtit.share.TranslationSource

/**
 * @author Joachim Daiber
 */

class BackoffLevel(val searcher: TranslationPairSearcher, val ranker: Option[TranslationPairRanker], val threshold: Double, val translationType: TranslationSource) {


  override def toString = "[%s, %s]".format(
      searcher match {
        case s: TranslationPairSearcherWrapper => { "%s (%d concurrent instances)".format(s.searchers.head.getClass.getSimpleName, s.size) }
        case s: TranslationPairSearcher => s.getClass.getSimpleName
      },
      ranker match {
        case Some(r) => r.getClass.getSimpleName
        case None => "no ranker"
      }
  )

}