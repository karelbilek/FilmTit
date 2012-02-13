package cz.filmtit.core.model

/**
 * Static class for identification of the source of a matches translation
 * pair.
 *
 * @author Joachim Daiber
 */

class TranslationSource(val name: String) {
  override def toString = name
}

object TranslationSource {
  def apply(name: String): TranslationSource = new TranslationSource(name)

  //Internal:
  val InternalExact = TranslationSource("Exact TM match")
  val InternalNE    = TranslationSource("NE based TM match")
  val InternalFuzzy = TranslationSource("Fuzzy TM match")

  //External:
  val ExternalTM    = TranslationSource("External TM match")
  val ExternalMT    = TranslationSource("External MT")

  //Unknown:
  val Unknown       = TranslationSource("Unkown source")


}
