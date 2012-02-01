package cz.filmtit.core.model
import cz.filmtit.core.model.Language._


/** A special case of [[cz.filmtit.core.model.TranslationPairStorage]], in which the
  * candidates are retrieved and indexed using a signature string.
  *
  * @author Joachim Daiber
  */
trait SignatureBasedStorage extends TranslationPairStorage {

  /** A signature String for a specific [[cz.filmtit.core.model.Chunk]] used to index and retrieve it. */
  def signature(sentence: Chunk, language: Language): String

  /** Write the signatures for the chunk table to the database. */
  def reindex()

}