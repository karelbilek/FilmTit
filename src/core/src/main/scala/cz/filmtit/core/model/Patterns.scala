package cz.filmtit.core.model


/**
 * Regular expression patterns used throughout the core.
 *
 * @author Joachim Daiber
 */

object Patterns {
  val number = "^[0-9]+".r
  val finalpunctuation = ".*([\\p{Punct}]+)$".r
  val punctuation = "[\\p{Punct}]+".r
}



