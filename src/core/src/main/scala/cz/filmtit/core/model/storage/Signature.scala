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

package cz.filmtit.core.model.storage

import cz.filmtit.share.annotations._
import cz.filmtit.core.model.data._
import cz.filmtit.core.model.data.ChunkUtils.toAnnotatedString
import cz.filmtit.share.Chunk
import scala.collection.JavaConversions._

import collection.mutable.ListBuffer

/**
 * Wrapper class for database signature Strings.
 *
 * A signature string is a representation of a Chunk that is used
 * to index the chunks in the database. A Signature can have
 * annotations, if the [[cz.filmtit.core.model.storage.TranslationPairStorage]]
 * is reversible, the annotations will be written to the database.
 *
 * @author Joachim Daiber
 */

class Signature(val surfaceform: String) {

  lazy val annotations = ListBuffer[Annotation]()

  def listAnnotations(): String = annotations.map(t => ("%s,%d,%d").format(t.getType.getDescription, t.getBegin, t.getEnd)).mkString(",")

}

object Signature {
  implicit def fromString(string: String): Signature = new Signature(string)

  /**
   * Create the Signature from a Chunk which may contain annotations.
   *
   * @param chunk chunk to be indexed, including annotations
   * @return
   */

   //implicit conversion to ChunkUtils
  def fromChunk(chunk: Chunk) = {
    val s = new Signature(toAnnotatedString(chunk))
    s.annotations ++= chunk.getAnnotations
    s
  }

  /**
   * Create the signature from the signature String and annotations that
   * are stored in the database.
   *
   * @param signature
   * @param annotations
   * @return
   */
  def fromDatabase(signature: String, annotations: String): Signature = {
    val sig = new Signature(signature)
    if (!annotations.equals(""))
      sig.annotations ++= annotations.split(",").sliding(3) map {
        (a => new Annotation(AnnotationType.fromDescription(a(0)), a(1).toInt, a(2).toInt))
      }

    sig
  }

}
