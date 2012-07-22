package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseSignatureStorage
import cz.filmtit.core.model._

import java.sql.Connection
import storage.Signature
import java.lang.String
import cz.filmtit.share._
import opennlp.tools.tokenize.Tokenizer


/**
 * Simple exact signature based translation pair storage using the
 * first letters of words in the chunk as a signature for indexing.
 *
 * @author Joachim Daiber
 */

class FirstLetterStorage(
  l1: Language,
  l2: Language,
  connection: Connection,
  tokenizerL1: Tokenizer,
  tokenizerL2: Tokenizer,
  useInMemoryDB: Boolean = false
) extends BaseSignatureStorage(
  l1,
  l2,
  TranslationSource.INTERNAL_EXACT,
  "sign_firstletter",
  connection,
  useInMemoryDB
) {

  def  tokenizer(language: Language): Tokenizer =
    if(language == l1) tokenizerL1 else tokenizerL2

  /**
   * Use the lowercased first letter of each word in the sentence as the signature.
   */
  override def signature(chunk: Chunk, language: Language): Signature = {
    val tokens: Array[String] = tokenizer(language).tokenize(chunk.getSurfaceForm)

    tokens map {
      token =>
        token match {
          case Patterns.number() => '0'
          case Patterns.punctuation() => null
          case _ => {
            token.take(
              tokens.size match {
                case 1 => token.length
                case 2 => 3
                case 3 => 2
                case _ => 1
              }
            ).toLowerCase
          }
        }
    } filter(_ != null) mkString (" ")
  }

  override def name: String = "Translation pair storage using the first letter of every word as an index."

}



