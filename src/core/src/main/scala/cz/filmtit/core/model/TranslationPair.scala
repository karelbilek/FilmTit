package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 *
 */

class TranslationPair(val source: Chunk, val target: Chunk, var mediaSource: MediaSource) {

  def this(source: Chunk, target: Chunk) {
    this(source, target, null)
  }

  override def toString: String = source + ", " + target

}

object TranslationPair {

  implicit def fromString(string: String): TranslationPair = {
    val splitString = string.trim().split("\t")

    splitString.length match {
      case 2 => new TranslationPair(splitString(0), splitString(1))
      case _ => null
    }

  }


}