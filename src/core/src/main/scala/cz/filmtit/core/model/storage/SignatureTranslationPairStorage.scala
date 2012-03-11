package cz.filmtit.core.model.storage

import cz.filmtit.core.model.data.Chunk
import cz.filmtit.core.model.Language

/**A special case of [[cz.filmtit.core.model.TranslationPairStorage]], in which the
 * candidates are retrieved and indexed using a signature string.
 *
 * @author Joachim Daiber
 */
trait SignatureTranslationPairStorage extends TranslationPairStorage {

  /**A signature String for a specific [[cz.filmtit.core.model.Chunk]] used to index and retrieve it. */
  def signature(sentence: Chunk, language: Language): Signature

  /**Write the signatures for the chunk table to the database. */
  def reindex()

}