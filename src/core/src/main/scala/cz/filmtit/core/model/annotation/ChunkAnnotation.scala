package cz.filmtit.core.model.annotation
import scala.collection.mutable.HashMap

/**
 * @author Joachim Daiber
 *
 */

class ChunkAnnotation(val name: String, val id: String) {
  override def toString = name
}

object ChunkAnnotation {
  val types = new HashMap[String, ChunkAnnotation]()
  
  def apply(n: String, id: String): ChunkAnnotation = {
    val ann = new ChunkAnnotation(n, id)
    types.put(id, ann)
    ann
  }
  
  def fromID(id: String) = types.get(id)
}