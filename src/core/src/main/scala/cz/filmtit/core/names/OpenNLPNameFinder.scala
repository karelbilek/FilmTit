package cz.filmtit.core.names

import opennlp.tools.util.Span
import cz.filmtit.core.model.names.NERecognizer
import opennlp.tools.tokenize.Tokenizer
import cz.filmtit.share.annotations._
import opennlp.tools.namefind.{TokenNameFinder, TokenNameFinderModel}
import cz.filmtit.share.Chunk

/**
 * Simple NE recognizer based on OpenNLP ME models.
 *
 * @author Joachim Daiber
 */

class OpenNLPNameFinder(
  val neType: AnnotationType,
  val nameFinder: TokenNameFinder,
  val tokenizer: Tokenizer
) extends NERecognizer(neType) {


  override def detect(chunk: Chunk) {

    val tokenized = tokenizer.tokenize(chunk.getSurfaceForm)
    val tokenizedPos = tokenizer.tokenizePos(chunk.getSurfaceForm)

    nameFinder.find(tokenized) foreach {
      name: Span => chunk.addAnnotation( new Annotation(
          neType,
          tokenizedPos(name.getStart).getStart,
          tokenizedPos(name.getEnd - 1).getEnd
      ))
    }

    nameFinder.clearAdaptiveData()
   
  }


}
