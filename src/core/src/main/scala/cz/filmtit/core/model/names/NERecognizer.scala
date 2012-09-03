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

package cz.filmtit.core.model.names

import cz.filmtit.share.Chunk
import cz.filmtit.share.annotations.AnnotationType

/**
 * Interface for named entity recognizers. Named Entities should be added as annotations
 * to the [[cz.filmtit.share.Chunk]].
 *
 * @author Joachim Daiber
 */

abstract class NERecognizer(val neClass: AnnotationType) {


  /**
   * Recognize named entities and add them as annotations to the chunk.
   *
   * @param chunk chunk on which NER will be performed
   */

  def detect(chunk: Chunk)

}
