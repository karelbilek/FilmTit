package cz.filmtit.core.concurrency.moses

import opennlp.tools.tokenize.Tokenizer
import akka.actor.Actor
import java.io.IOException
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl

/**
 * @author Joachim Daiber
 */



class MosesActor(val client:XmlRpcClient) extends Actor {

  def receive = {
    case MosesRequest(source) => {
        val mosesParams = new java.util.HashMap[String,String]()
	    mosesParams.put("text", source)
	    mosesParams.put("align", "false")
	    mosesParams.put("report-all-factors", "false")
        val params = Array[Object](null)
	    params(0) = mosesParams


	    val result:java.util.HashMap[String, Object] = client.execute("translate", params) match {
            case m:java.util.HashMap[String, Object]=>m
            case _ => throw new ClassCastException("Wrong type of result from moses")
        }
	
        val translation = result.get("text") match {
            case s:String=> s
            case _ => throw new ClassCastException("Wrong type of result from moses")
        }
        
        sender ! translation
 
     }

    
  }

 /*override def preRestart(reason: Throwable, message: Option[Any]) {
    message foreach { self forward _ }
  }*/

}
