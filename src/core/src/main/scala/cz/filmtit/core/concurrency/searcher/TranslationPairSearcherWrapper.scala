package cz.filmtit.core.concurrency.searcher

import cz.filmtit.core.model.TranslationPairSearcher
import cz.filmtit.share.{Chunk, TranslationPair, Language}
import akka.pattern.ask
import akka.dispatch.Await
import akka.util.Timeout
import akka.util.duration._
import akka.routing.SmallestMailboxRouter
import java.io.IOException
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{OneForOneStrategy, ActorSystem, Props}


/**
 * @author Joachim Daiber
 */

class TranslationPairSearcherWrapper(val searchers: List[TranslationPairSearcher], val searcherTimeout: Int)
  extends TranslationPairSearcher(searchers.head.l1, searchers.head.l2, readOnly = true) {

  val system = ActorSystem()
  val workers = searchers map { searcher =>
    system.actorOf(Props(new TranslationPairSearcherActor(searcher)))
  }

  def size: Int = searchers.size

  val router = system.actorOf(Props[TranslationPairSearcherActor].withRouter(
    SmallestMailboxRouter(routees = workers).withSupervisorStrategy(
      OneForOneStrategy(maxNrOfRetries = 10) {
        case _: IOException => Restart
      })
     )
  )

  //some heavy scala magic
  implicit val timeout = Timeout(searcherTimeout seconds)

  /**
   * Retrieve a list of candidate translation pairs from a database or
   * service.
   */
  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val futureResult = router ? SearcherRequest(chunk, language)
    Await.result(futureResult, timeout.duration).asInstanceOf[List[TranslationPair]]
  }

}
