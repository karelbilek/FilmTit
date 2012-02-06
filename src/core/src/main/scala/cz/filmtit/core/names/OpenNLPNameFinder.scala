package cz.filmtit.core.names

import opennlp.tools.namefind.TokenNameFinder
import opennlp.tools.util.Span

import cz.filmtit.core.model.names.NERecognizer
import opennlp.tools.tokenize.Tokenizer
import cz.filmtit.core.model.annotation.ChunkAnnotation
import cz.filmtit.core.model.Chunk


/**
 * @author Joachim Daiber
 *
 *
 *
 */

class OpenNLPNameFinder(
  val neType: ChunkAnnotation,
  val nameFinder: TokenNameFinder,
  val tokenizer: Tokenizer
) extends NERecognizer(neType) {

  override def detect(chunk: Chunk): Chunk = {

    val tokenized = tokenizer.tokenize(chunk.surfaceform)
    val tokenizedPos = tokenizer.tokenizePos(chunk.surfaceform)

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
