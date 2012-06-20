package cz.filmtit.core.model.storage

import cz.filmtit.share.Chunk
import cz.filmtit.share.Language

/**A special case of [[cz.filmtit.core.model.TranslationPairStorage]], in which the
 * candidates are retrieved and indexed using a signature string.
 *
 * @author Joachim Daiber
 */
trait SignatureTranslationPairStorage extends TranslationPairStorage {

  /**A signature String for a specific [[cz.filmtit.share.Chunk]] used to index and retrieve it. */
  def signature(sentence: Chunk, language: Language): Signature

}
