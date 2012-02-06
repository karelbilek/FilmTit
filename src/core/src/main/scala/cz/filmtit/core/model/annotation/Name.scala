package cz.filmtit.core.model.annotation

/**
 * @author Joachim Daiber
 *
 */

class NameType(name: String) extends ChunkAnnotation {
  override def toString = name
}

object NameType {
  def apply(n: String): NameType = new NameType(n)
  def apply(): NameType = new NameType("")
}

object Name {
  val Person = NameType("Person")
  val Place = NameType("Place")
  val Organization = NameType("Organization")
}

