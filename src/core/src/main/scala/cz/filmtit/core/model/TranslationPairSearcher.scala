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
