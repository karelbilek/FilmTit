package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 *
 * TODO: Movie source? Data from IMDB?
 */

class TranslationPair(val source: Chunk, val target: Chunk)

object TranslationPair {

  implicit def fromString(string: String): TranslationPair = {
    val splitString = string.trim().split("\t")
    new TranslationPair(splitString(0), splitString(1))
  }

}