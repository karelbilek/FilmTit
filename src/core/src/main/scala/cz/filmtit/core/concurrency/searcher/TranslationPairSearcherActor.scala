package cz.filmtit.core.concurrency.searcher

import cz.filmtit.core.model.TranslationPairSearcher
import akka.actor.Actor
import java.io.IOException
import cz.filmtit.share.exceptions.{SearcherNotAvailableException, LanguageNotSupportedException}
import akka.actor.SupervisorStrategy.Escalate

/**
 * An Actor for [[cz.filmtit.core.model.TranslationPairSearcher]]s, used to retrieve
 * translation pair candidates in parallel.
 *
 * @author Joachim Daiber
 * @author Karel Bilek
 */

class TranslationPairSearcherActor(val searcher: TranslationPairSearcher) extends Actor {

  /**
   * Method for processing requests to the searcher.
   * @return
   */
  def receive = {
    case SearcherRequest(chunk, language) => {
      try {
        sender ! searcher.candidates(chunk, language)
      } catch {
        case ex: Exception => sender ! akka.actor.Status.Failure(ex)
      }
    }
  }

  /**
   * Method called on a crashed Actor, forward all exceptions.
   *
   * @param reason the Throwable that caused the restart to happen
   * @param message optionally the current message the actor processed when failing, if applicable
   */
  override def preRestart(reason: Throwable, message: Option[Any]) {
    message foreach { self forward _ }
  }

}
