package cz.filmtit.core.model.annotation
import scala.collection.mutable.HashMap

/**
 * A chunk annotation describes a substring in a Chunk, e.g.
 * as a Named Entity of type "Person".
 *
 * @author Joachim Daiber
 */

class ChunkAnnotation(val name: String, val id: String) {
  override def toString = name
}


object ChunkAnnotation {

  /**
   * Get the ChunkAnnotation corresponding to the unique
   * identifier.
   */
  val fromID = new HashMap[String, ChunkAnnotation]()

  /**
   * Get the ChunkAnnotation corresponding to the unique
   * name.
   */
  val fromName = new HashMap[String, ChunkAnnotation]()

  /**
   * Instantiate new ChunkAnnotation. All ChunkAnnotation
   * Singleton objects are stored in a HashMap, so they can
   * later be retrieved by their id.
   *
   * @param name name of the annotation
   * @param id unique identifier for the annotation
   * @return
   */
  def apply(name: String, id: String): ChunkAnnotation = {
    val ann = new ChunkAnnotation(name, id)
    fromID.put(id, ann)
    fromName.put(name, ann)
    ann
  }


  //HACK, all names must be initialized to be able to query
  //them with fromID, fromName
  Name.Person
}