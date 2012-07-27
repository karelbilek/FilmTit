package cz.filmtit.core.model

import cz.filmtit.core.model.storage.MediaStorage
import cz.filmtit.share.{Language, MediaSource, TranslationPair, Chunk}

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
            n: Int = 10, inner: Boolean = false): List[TranslationPair]


  /**
   * Retrieve only the best translation candidate for a chunk in language l,
   * with given MediaResource.
   *
   * @param chunk the chunk to be queried
   * @param language the language of the chunk
   * @param mediaSource the media source from which the chunk is taken
   * @return
   */
  def firstBest(chunk: Chunk, language: Language, mediaSource: MediaSource):
  Option[TranslationPair]


  /**
   * Retrieve an instance of a MediaStorage which allows access to
   * information in the database about the sources of translation pairs.
   *
   * @return
   */
  def mediaStorage: MediaStorage


  def close()
}
