package cz.filmtit.core.concurrency.tokenizer

import opennlp.tools.tokenize.Tokenizer
import akka.actor.Actor
import java.io.IOException

/**
 * An Actor for tokenizing [[cz.filmtit.share.Chunk]]s.
 *
 * @author Joachim Daiber
 * @author Karel Bilek
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


}
