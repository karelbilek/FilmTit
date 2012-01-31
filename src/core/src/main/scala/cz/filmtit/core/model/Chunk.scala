package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 */

class Chunk(val surfaceform: String) {
  override def toString: String = this.surfaceform

  override def hashCode = this.surfaceform.hashCode()
  override def equals(other: Any) = this.surfaceform.equals(other.toString)

}


object Chunk {

  implicit def fromString(string: String): Chunk = new Chunk(string)
  implicit def toString(chunk: Chunk): String = chunk.toString

}

