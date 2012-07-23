package cz.filmtit.core.concurrency.searcher

import cz.filmtit.core.model.TranslationPairSearcher
import akka.actor.Actor
import java.io.IOException

/**
 * @author Joachim Daiber
 */

class TranslationPairSearcherActor(val searcher: TranslationPairSearcher) extends Actor {

  def receive = {
    case SearcherRequest(chunk, language) => {
      try {
        sender ! searcher.candidates(chunk, language)
      } catch {
        case e: NullPointerException => throw new IOException("Could not run NER.")
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    message foreach { self forward _ }
  }

}
