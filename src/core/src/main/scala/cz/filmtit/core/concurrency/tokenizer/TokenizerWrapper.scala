package cz.filmtit.core.concurrency.tokenizer

import opennlp.tools.util.Span
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
import collection.parallel.ParSeq

import opennlp.tools.tokenize.Tokenizer

/**
 * @author Joachim Daiber
 */

class TokenizerWrapper(val tokenizers: ParSeq[Tokenizer], val tokenizerTimeout: Int) {

  val system = ActorSystem()
  val workers = tokenizers.map { case tokenizer:Tokenizer =>
    system.actorOf(Props(new TokenizerActor(tokenizer)))
  }.seq

  def size: Int = tokenizers.size

  val router = system.actorOf(Props[TokenizerActor].withRouter(
    SmallestMailboxRouter(routees = workers).withSupervisorStrategy(
      OneForOneStrategy(maxNrOfRetries = 10) {
        case _: IOException => Restart
      })
     )
  )

  //some heavy scala magic
  implicit val timeout = Timeout(tokenizerTimeout seconds)

  def tokenizePos(chunk: Chunk):Array[Span] = {
    val futureResult = router ? TokenizerRequestPos(chunk)
    Await.result(futureResult, timeout.duration) match {
        case e:Array[Span]=>e
        case _ => throw new Exception("Wrong result from tokenizePos");
    }
  }

  def tokenize(chunk: Chunk) {
    
    val futureResult = router ? TokenizerRequestNormal(chunk)
    Await.result(futureResult, timeout.duration) //this is thrown away

  }

  def close() {
    system.shutdown()
  }

}
