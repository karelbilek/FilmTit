/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseSignatureStorage
import cz.filmtit.core.model._

import java.sql.Connection
import storage.Signature
import java.lang.String
import cz.filmtit.share._


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
  useInMemoryDB: Boolean = false
) extends BaseSignatureStorage(
  l1,
  l2,
  TranslationSource.INTERNAL_EXACT,
  "sign_firstletter",
  connection,
  useInMemoryDB
) {

  /**
   * Use the lowercased first letter of each word in the sentence as the signature.
   */
  override def signature(chunk: Chunk, language: Language): Signature = {

    val tokens: Array[String] = chunk.getTokens filter {
      case Patterns.punctuation() => false
      case _ => true
    }

    tokens map {
      token =>
        token match {
          case Patterns.number() => '0'
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



