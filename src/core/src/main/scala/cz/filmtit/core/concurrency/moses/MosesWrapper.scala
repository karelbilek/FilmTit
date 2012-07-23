package cz.filmtit.core.concurrency.moses

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
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl

/**
 * @author Joachim Daiber
 */

class MosesWrapper(url:java.net.URL, maxNumber:Int, mosesTimeout:Int) {
  
  val config = new XmlRpcClientConfigImpl();
  config.setServerURL(url);
  val client = new XmlRpcClient();
  client.setConfig(config);

  val system = ActorSystem()
  val workers = (1 to maxNumber).map{_ =>
    system.actorOf(Props(new MosesActor(client)))
  }


  val router = system.actorOf(Props[MosesActor].withRouter(
    SmallestMailboxRouter(routees = workers).withSupervisorStrategy(
      OneForOneStrategy(maxNrOfRetries = 10) {
        case _: IOException => Restart
      })
     )
  )

  //some heavy scala magic
  implicit val timeout = Timeout(mosesTimeout seconds)

  def translate(source: String):String = {
    val futureResult = router ? MosesRequest(source)
    Await.result(futureResult, timeout.duration) match {
        case target:String=>target
        case _ => throw new Exception("Wrong result from tokenizePos");
    }
  }
   


}
