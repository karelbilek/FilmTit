/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
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
