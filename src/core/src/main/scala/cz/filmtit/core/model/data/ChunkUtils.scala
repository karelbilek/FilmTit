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

package cz.filmtit.core.model.data

import scala.collection.JavaConversions._
import cz.filmtit.share.annotations.AnnotationType
import cz.filmtit.share.Chunk

/**
 * Core-specific methods for working with [[cz.filmtit.share.Chunk]] objects.
 */
object ChunkUtils {

  /**
   * Converts the chunk into a String using the function specified as the format parameter.
   *
   * @param chunk the chunk to be converted
   * @param format a function converting each instance of an annotation to a String
   * @return
   */
 def toAnnotatedString(
    chunk: Chunk,
    format: (AnnotationType, String) => String = 
        { (t, _) => "<" + t.getDescription + ">" }
  ): String = {
 
    val surfaceform = chunk.getSurfaceForm

    var lastOffset = 0
    (chunk.getAnnotations map {
      annotation => {
        val anType = annotation.getType
        val from = annotation.getBegin
        val to = annotation.getEnd
        
        "%s%s".format(
        surfaceform.substring(lastOffset, from),
        format(anType,
          surfaceform.substring(from, math.min(surfaceform.size, to))), {
          lastOffset = to
        }
        )
      }
    }).mkString + surfaceform.substring(math.min(surfaceform.size, lastOffset))

  }


}
