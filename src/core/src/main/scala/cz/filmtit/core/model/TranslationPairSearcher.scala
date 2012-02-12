package cz.filmtit.core.model

import cz.filmtit.core.model.data.{TranslationPair, Chunk}

/**
 * @author Joachim Daiber
 */

abstract class TranslationPairSearcher(val l1: Language, val l2: Language) {

  /**Retrieve a list of candidate translation pairs from a database or
   * service.
   */
  def candidates(chunk: Chunk, language: Language): List[TranslationPair]

}
