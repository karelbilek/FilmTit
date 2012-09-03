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

import _root_.java.util
import cz.filmtit.core.model.storage.MediaStorage
import cz.filmtit.share._

/**
 * A Translation Memory.
 *
 * This is the general interface for translation memories.
 *
 * @author Joachim Daiber
 */

trait TranslationMemory {

  /**
   * Initially fill the translation memory with a number of translation
   * pairs.
   *
   * @param pairs training translation pairs
   */
  def add(pairs: Array[TranslationPair])


  /**
   * Recreate the non-content tables and indexes for retrieving the translation
   * pairs.
   */
  def reindex()

  /**
   * Warm up the TranslationMemory (searchers may or may not have a warm-up routine)
   */
  def warmup()

  /**
   * Reset the TranslationMemory (this will clear all database tables!)
   */
  def reset()

  /**
   * Notify the TM to finish the import.
   */
  def finishImport()


  /**
   * Retrieve n (by default 10) best candidates for a Chunk c in language l,
   * which is from a given MediaSource.
   *
   * @param chunk the chunk to be queried
   * @param language the language of the chunk
   * @param mediaSource the media source from which the chunk is taken
   * @param n number of translations (10 by default)
   * @return
   */
  def nBest(chunk: Chunk, language: Language, mediaSource: MediaSource,
            n: Int = 10, inner: Boolean = false,
            forbiddenSources: java.util.Set[TranslationSource] = util.Collections.emptySet[TranslationSource]): List[TranslationPair]


  /**
   * Retrieve only the best translation candidate for a chunk in language l,
   * with given MediaResource.
   *
   * @param chunk the chunk to be queried
   * @param language the language of the chunk
   * @param mediaSource the media source from which the chunk is taken
   * @return
   */
  def firstBest(chunk: Chunk, language: Language, mediaSource: MediaSource,
                forbiddenSources: java.util.Set[TranslationSource] = util.Collections.emptySet[TranslationSource]):
  Option[TranslationPair]


  /**
   * Retrieve an instance of a MediaStorage which allows access to
   * information in the database about the sources of translation pairs.
   *
   * @return
   */
  def mediaStorage: MediaStorage

  /**
   * Tokenize the Chunk with the tokenizer corresponding to its language.
   *
   * @param chunk the chunk to be tokenized
   * @param language the language of the chunk
   */
  def tokenize(chunk:Chunk, language:Language)

  /**
   * Closes the translation memory (perform all required actions, e.g. closing the database connection).
   */
  def close()
}
