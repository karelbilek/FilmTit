package cz.filmtit.core.concurrency.searcher

import cz.filmtit.core.model.TranslationPairSearcher
import akka.actor.Actor
import java.io.IOException
import cz.filmtit.share.exceptions.{SearcherNotAvailableException, LanguageNotSupportedException}
import akka.actor.SupervisorStrategy.Escalate

/**
 * @author Joachim Daiber
 */

class TranslationPairSearcherActor(val searcher: TranslationPairSearcher) extends Actor {

  def receive = {
    case SearcherRequest(chunk, language) => {
      try {
        sender ! searcher.candidates(chunk, language)
      } catch {
        case ex: Exception => sender ! akka.actor.Status.Failure(ex)
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    message foreach { self forward _ }
  }

}
