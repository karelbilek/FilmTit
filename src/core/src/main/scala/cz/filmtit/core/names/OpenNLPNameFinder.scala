/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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
