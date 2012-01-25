package cz.filmtit.core.database

import cz.filmtit.core.model._
import cz.filmtit.core.model.Language._



/**
 * @author Joachim Daiber
 *
 */

class PostgresFirstLetterStorage extends PostgresSignatureBasedStorage {

  /**
   * Use the lowercased first letter of each word in the sentence as the signature.
   */
  override def signature(chunk: Chunk, language: Language): String = {
    new String(chunk.surfaceform.split("[ ,.?!-]") filter (_ != "") map {
      token =>
        token match {
          case Patterns.number() => '0'
          case _ => {
            if (Patterns.isStopWord(token, language))
              '_'
            else
              token.head.toLower
          }
        }
      }
    )
  }

  override def name: String = "Translation pair storage using the first letter of every word as an index."

}



