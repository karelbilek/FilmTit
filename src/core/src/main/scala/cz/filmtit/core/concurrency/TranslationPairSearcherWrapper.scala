package cz.filmtit.core.concurrency

import cz.filmtit.core.model.TranslationPairSearcher
import cz.filmtit.share.{Chunk, TranslationPair, Language}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.routing.RoundRobinRouter
import akka.dispatch.Await
import akka.util.Timeout
import akka.util.duration._


/**
 * @author Joachim Daiber
 *
 *
 *
 */

class TranslationPairSearcherWrapper(val searchers: List[TranslationPairSearcher])
  extends TranslationPairSearcher(searchers.head.l1, searchers.head.l2, readOnly = true) {

  val system = ActorSystem()
  val workers = searchers map { searcher =>
    system.actorOf(Props(new TranslationPairSearcherActor(searcher)))
  }

  val router = system.actorOf(Props[TranslationPairSearcherActor].withRouter(
    RoundRobinRouter(routees = workers)
  ))
  implicit val timeout = Timeout(30 seconds)

  /**
   * Retrieve a list of candidate translation pairs from a database or
   * service.
   */
  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val futureResult = router ? SearcherRequest(chunk, language)
    Await.result(futureResult, timeout.duration).asInstanceOf[List[TranslationPair]]
  }



}
