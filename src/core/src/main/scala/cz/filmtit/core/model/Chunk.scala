package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 */

class Chunk(val surfaceform: String)

object Chunk {
  implicit def toChunk(string: String): Chunk = {
    new Chunk(string)
  }

  implicit def toString(chunk: Chunk): String = {
    chunk.surfaceform
  }

}

