package cz.filmtit.core.model

import cz.filmtit.core.model.data.{TranslationPair, Chunk}
import cz.filmtit.core.model.Language._

/**
 * @author Joachim Daiber

 */

abstract class TranslationPairSearcher {

  /**Retrieve a list of candidate translation pairs from the database.
   * Depending on the implementation, the pairs may have a
   * [[cz.filmtit.core.model.ScoredTranslationPair# c a n i d a t e S c o r e]]
   */
  def candidates(chunk: Chunk, language: Language): List[TranslationPair]

}
