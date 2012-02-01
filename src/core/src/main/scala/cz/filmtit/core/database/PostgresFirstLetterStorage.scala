package cz.filmtit.core.database

import cz.filmtit.core.model._
import cz.filmtit.core.model.Language._
import java.lang.String


/**
 * @author Joachim Daiber
 *
 */

class PostgresFirstLetterStorage(l1: Language, l2: Language)
  extends PostgresSignatureBasedStorage(l1, l2, "sign_firstletter") {

  /**
   * Use the lowercased first letter of each word in the sentence as the signature.
   */
  override def signature(chunk: Chunk, language: Language): String = {
    val tokens: Array[String] = chunk.surfaceform.split("[ ,.?!-]") filter (_ != "")

    tokens map {
      token =>
        token match {
          case Patterns.number() => '0'
          case _ => {
            token.take(
              tokens.size match {
                case 1 => 4
                case 2 => 3
                case 3 => 2
                case _ => 1
              }
            ).toLowerCase
          }
        }
    } mkString(" ")
  }

  override def name: String = "Translation pair storage using the first letter of every word as an index."

}



