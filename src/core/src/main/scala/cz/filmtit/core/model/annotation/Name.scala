package cz.filmtit.core.model.annotation

/**
 * @author Joachim Daiber
 *
 */

object Name {
  val Person =       ChunkAnnotation("Person", "NE_Per")
  val Place =        ChunkAnnotation("Place", "NE_Pla")
  val Organization = ChunkAnnotation("Organization", "NE_Org")
}
