package cz.filmtit.core.model

/** A special case of [[TranslationPairStorage]], in which the
  * candidates are retrieved and indexed using a signature string.
  *
  * @author Joachim Daiber
  */
trait SignatureBasedStorage extends TranslationPairStorage {

  /** A signature String for a specific [[Chunk]] used to index and retrieve it. */
  def signature(sentence: Chunk): String

}