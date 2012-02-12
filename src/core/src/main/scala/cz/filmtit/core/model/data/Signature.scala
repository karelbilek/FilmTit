package cz.filmtit.core.model.data

import cz.filmtit.core.model.annotation.ChunkAnnotation
import collection.mutable.ListBuffer

/**
 * @author Joachim Daiber
 *
 *
 *
 */

class Signature(val surfaceform: String) {

  lazy val annotations = ListBuffer[Triple[ChunkAnnotation, Int, Int]]()

  def listAnnotations(): String = annotations.map( t => ("%s,%d,%d").format(t._1.id,t._2,t._3) ).mkString(",")
  
}

object Signature {
  implicit def fromString(string: String): Signature = new Signature(string)

  def fromChunk(chunk: Chunk) = {
    val s = new Signature(chunk.toAnnotatedString())
    s.annotations ++= chunk.annotations
    s
  }

  def fromDatabase(signature: String, annotations: String): Signature = {
    val sig = new Signature(signature)
    if (!annotations.equals(""))
      sig.annotations ++= annotations.split(",").sliding(3) map {
        ( a => (ChunkAnnotation.fromID(a(0)).get, a(1).toInt, a(2).toInt) )
      }

    sig
  }

}