package cz.filmtit.core.names

import opennlp.tools.namefind.{TokenNameFinder}
import opennlp.tools.util.Span

import cz.filmtit.core.model.names.NEType.NEType
import cz.filmtit.core.model.names.{NERecognizer}
import cz.filmtit.core.model.{Chunk}
import opennlp.tools.tokenize.Tokenizer


/**
 * @author Joachim Daiber
 *
 *
 *
 */

class OpenNLPNameFinder(
  val neType: NEType,
  val nameFinder: TokenNameFinder,
  val tokenizer: Tokenizer
) extends NERecognizer(neType) {

  override def detect(chunk: Chunk): Chunk = {

    val tokenized = tokenizer.tokenize(chunk.toString)
    val tokenizedPos = tokenizer.tokenizePos(chunk.toString)

    val find: Array[Span] = nameFinder.find(tokenized)

    nameFinder.find(tokenized) foreach {
      name: Span => {
        
        val annotation = (neType,
                          tokenizedPos(name.getStart).getStart,
                          tokenizedPos(name.getEnd - 1).getEnd)
        
        assert(annotation._2 <= annotation._3)
        chunk.annotations += annotation
      }
    }

    chunk
  }


}
