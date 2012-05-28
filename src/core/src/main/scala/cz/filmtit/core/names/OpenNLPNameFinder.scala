package cz.filmtit.core.names

import opennlp.tools.util.Span
import cz.filmtit.core.model.names.NERecognizer
import opennlp.tools.tokenize.Tokenizer
import cz.filmtit.core.model.annotation.ChunkAnnotation
import cz.filmtit.core.model.data.AnnotatedChunk
import opennlp.tools.namefind.{TokenNameFinder, TokenNameFinderModel}


/**
 * Simple NE recognizer based on OpenNLP ME models.
 *
 * @author Joachim Daiber
 */

class OpenNLPNameFinder(
  val neType: ChunkAnnotation,
  val model:TokenNameFinderModel,
  val nameFinder: TokenNameFinder,
  val tokenizer: Tokenizer
) extends NERecognizer(neType) {


  override def detect(chunk: AnnotatedChunk) {

    val tokenized = tokenizer.tokenize(chunk.surfaceform)
    val tokenizedPos = tokenizer.tokenizePos(chunk.surfaceform)

    nameFinder.find(tokenized) foreach {
      name: Span => chunk.addAnnotation(
          neType,
          tokenizedPos(name.getStart).getStart,
          tokenizedPos(name.getEnd - 1).getEnd
      )
    }

    nameFinder.clearAdaptiveData()
   
  }


}
