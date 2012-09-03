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
import cz.filmtit.share.{Chunk, TranslationPair, Language}
import akka.pattern.ask
import akka.dispatch.Await
import akka.util.Timeout
import akka.util.duration._
import akka.routing.SmallestMailboxRouter
import java.io.IOException
import akka.actor.SupervisorStrategy.{Escalate, Stop, Restart}
import akka.actor.{OneForOneStrategy, ActorSystem, Props}
import cz.filmtit.share.exceptions.{SearcherNotAvailableException, LanguageNotSupportedException}


/**
 * TranslationPairSearcherWrapper is a TranslationPairSearcher that can be constructed from a list of
 * [[cz.filmtit.core.model.TranslationPairSearcher]]s, which we call workers. It will distribute its
 * requests to its workers and return the result once it is produced by on of the workers.
 *
 * The Akka routing strategy is [[akka.routing.SmallestMailboxRouter]].
 *
 * @author Joachim Daiber
 * @author Karel Bilek
 */

class TranslationPairSearcherWrapper(val searchers: List[TranslationPairSearcher], val searcherTimeout: Int)
  extends TranslationPairSearcher(searchers.head.l1, searchers.head.l2, readOnly = true) {

  //The Actor system and workers.
  val system = ActorSystem()
  val workers = searchers map { searcher =>
    system.actorOf(Props(new TranslationPairSearcherActor(searcher)))
  }

  def size: Int = searchers.size

  //The router for routing requests to works
  val router = system.actorOf(Props[TranslationPairSearcherActor].withRouter(
    SmallestMailboxRouter(routees = workers).withSupervisorStrategy(
      OneForOneStrategy(maxNrOfRetries = 10) {
        case _: IOException => Restart
        case _: LanguageNotSupportedException => Stop
        case _: SearcherNotAvailableException => Stop
      })
     )
  )

  //Timeout for workers
  implicit val timeout = Timeout(searcherTimeout seconds)

  /**
   * Retrieve a list of candidate translation pairs from a database or
   * service.
   */
  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val futureResult = router ? SearcherRequest(chunk, language)
    Await.result(futureResult, timeout.duration).asInstanceOf[List[TranslationPair]]
  }

  override def close() {
    system.shutdown()
  }


  override def toString = "SearcherWrapper[%s, %d]".format(searchers.head.getClass.getSimpleName, searchers.size)

  override def requiresTokenization = searchers.head.requiresTokenization

}
