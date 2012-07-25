package cz.filmtit.core.concurrency.tokenizer

import opennlp.tools.tokenize.Tokenizer
import akka.actor.Actor
import java.io.IOException

/**
 * @author Joachim Daiber
 */

object TokenizerActor {
    val empty:Unit = ()
}

class TokenizerActor(val tokenizer:Tokenizer) extends Actor {

  def receive = {
    case TokenizerRequestPos(chunk) => {
      try {
         sender ! tokenizer.tokenizePos(chunk.getSurfaceForm)
 
      } catch {
        case e: NullPointerException => throw new IOException("Could not run NER.")
      }
    }

    case TokenizerRequestNormal(chunk) => {
      try {
        chunk.setTokens(tokenizer.tokenize(chunk.getSurfaceForm))
        sender ! TokenizerActor.empty
      } catch {
        case e: NullPointerException => throw new IOException("Could not run NER.")
      }
    }
    
  }

 /*override def preRestart(reason: Throwable, message: Option[Any]) {
    message foreach { self forward _ }
  }*/

}
