package cz.filmtit.core.model

import collection.mutable.ListBuffer
import names.NEType

/**
 * @author Joachim Daiber
 */

class Chunk(val surfaceform: String) {

  /**
   * Annotations are stored in a list but are only instantiated
   * if they are required (lazy).
   */
  lazy val annotations = ListBuffer[Triple[NEType.NEType, Int, Int]]()

  override def toString: String = this.surfaceform

  override def hashCode = this.surfaceform.hashCode()
  override def equals(other: Any) = this.surfaceform.equals(other.toString)



}


object Chunk {

  implicit def fromString(string: String): Chunk = new Chunk(string)
  implicit def toString(chunk: Chunk): String = chunk.toString

}

