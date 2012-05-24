package cz.filmtit.core.concurrency

import cz.filmtit.core.model.TranslationPairSearcher
import akka.actor.Actor
import cz.filmtit.share.{Chunk, Language}

/**
 * @author Joachim Daiber
 */

class TranslationPairSearcherActor(val searcher: TranslationPairSearcher) extends Actor {

  def receive = {
    case SearcherRequest(chunk, language) => sender tell searcher.candidates(chunk, language)
  }

}
