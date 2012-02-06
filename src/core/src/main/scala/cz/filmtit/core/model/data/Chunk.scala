package cz.filmtit.core.model.data

import collection.mutable.ListBuffer
import cz.filmtit.core.model.annotation.ChunkAnnotation

/**
 * @author Joachim Daiber
 */

class Chunk(val surfaceform: String) {

  /**
   * Annotations are stored in a list but are only instantiated
   * if they are required (lazy).
   */
  lazy val annotations = ListBuffer[Triple[ChunkAnnotation, Int, Int]]()

  override def toString: String = {
    if (this.annotations.size > 0)
      toAnnotatedString({
        (neType, surface) => "[%s]".format(surface)
      })
    else
      this.surfaceform
  }

  override def hashCode = this.surfaceform.hashCode()

  override def equals(other: Any) = this.surfaceform.equals(other.toString)

  def toAnnotatedString(
                         formatAnnotation: (ChunkAnnotation, String) => String
                         ): String = {

    var lastOffset = 0
    (this.annotations.toList map {
      triple => {
        val (annotation, from, to) = triple
        "%s%s".format(
        surfaceform.substring(lastOffset, from),
        formatAnnotation(annotation,
          surfaceform.substring(from, math.min(surfaceform.size - 1, to))), {
          lastOffset = to
        }
        )
      }
    }).mkString + surfaceform.substring(math.min(surfaceform.size - 1, lastOffset))

  }
}


object Chunk {

  implicit def fromString(string: String): Chunk = new Chunk(string)

  implicit def toString(chunk: Chunk): String = chunk.toString

}

