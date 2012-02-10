package cz.filmtit.core.model

import cz.filmtit.core.model.data.{Chunk, MediaSource, ScoredTranslationPair, TranslationPair}

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
  def initialize(pairs: Array[TranslationPair])


  /**
   * Recreate the non-content tables and indexes for retrieving the translation
   * pairs.
   */
  def reindex()

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
            n: Int = 10): List[ScoredTranslationPair]

  /**
   * Retrieve only the best tranlation candidate for a chunk in language l,
   * with given MediaResource.
   *
   * @param chunk the chunk to be queried
   * @param language the language of the chunk
   * @param mediaSource the media source from which the chunk is taken
   * @return
   */
  def firstBest(chunk: Chunk, language: Language, mediaSource: MediaSource):
  Option[ScoredTranslationPair]

  /**
   * TODO refactor this
   * @param mediaSource
   * @return
   */
  def addMediaSource(mediaSource: MediaSource): Long


}