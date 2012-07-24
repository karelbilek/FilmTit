package cz.filmtit.core.names

import cz.filmtit.core.concurrency.tokenizer.TokenizerWrapper

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
  val tokenizer: TokenizerWrapper
) extends NERecognizer(neType) {


  override def detect(chunk: Chunk) {

    //val tokenized = tokenizer.tokenize(chunk.getSurfaceForm)
    if (!chunk.isTokenized) {
        tokenizer.tokenize(chunk)
    }
    val tokenized = chunk.getTokens
    val tokenizedPos = tokenizer.tokenizePos(chunk)

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
