package cz.filmtit.core.model.data

import cz.filmtit.core.model.annotation.ChunkAnnotation
import collection.mutable.ListBuffer

/**
 * Wrapper class for signature Strings.
 *
 * A signature string is a representation of a Chunk that is used
 * to index the chunks in the database. A Signature can have
 * annotations, if the [[cz.filmtit.core.model.storage.TranslationPairStorage]]
 * is reversible, the annotations will be wrote to the database.
 *
 * @author Joachim Daiber
 */

class Signature(val surfaceform: String) {

  lazy val annotations = ListBuffer[Triple[ChunkAnnotation, Int, Int]]()

  def listAnnotations(): String = annotations.map( t => ("%s,%d,%d").format(t._1.id,t._2,t._3) ).mkString(",")
  
}

object Signature {
  implicit def fromString(string: String): Signature = new Signature(string)

  /**
   * Create the Signature from a Chunk which may contain annotations.
   *
   * @param chunk chunk to be indexed, including annotations
   * @return
   */
  def fromChunk(chunk: Chunk) = {
    val s = new Signature(chunk.toAnnotatedString())
    s.annotations ++= chunk.annotations
    s
  }

  /**
   * Create the signature from the signature and annotations that
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
        ( a => (ChunkAnnotation.fromID(a(0)).get, a(1).toInt, a(2).toInt) )
      }

    sig
  }

}