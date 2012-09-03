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

package cz.filmtit.core.model

import cz.filmtit.share.{Language, TranslationPair, Chunk}


/**
 * Interface for translation pair searchers, based on a local database,
 * external services, etc.
 *
 * @author Joachim Daiber
 */

abstract class TranslationPairSearcher(
  val l1: Language,
  val l2: Language,
  readOnly: Boolean = true
) {

  /**
   * Get the correct chunk for the language from the translation pair.
   */
  def chunkForLanguage(pair: TranslationPair, language: Language): Chunk = {
    if (language equals l1)
      pair.getChunkL1
    else
      pair.getChunkL2
  }

  /**
   * Retrieve a list of candidate translation pairs from a database or
   * service.
   *
   * @param chunk the chunk to be queried
   * @param language the language of the query
   * @return translation pair candidates for the chunk
   */
  def candidates(chunk: Chunk, language: Language): List[TranslationPair]

  /**
   * Closes the searcher (perform all required actions, e.g. closing the database connection).
   */
  def close()

  /**
   * Indicates whether the searcher requires the chunks to be tokenized.
   *
   * @return <code>true</code> if the searcher requires the chunk to be tokenized.
   */
  def requiresTokenization: Boolean

}
