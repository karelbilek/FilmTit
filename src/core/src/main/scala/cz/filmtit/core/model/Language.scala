package cz.filmtit.core.model

/**
 * Static object for identification of languages. A language has a name
 * and an ISO code.
 *
 * @author Joachim Daiber
 */

class Language(val name: String, val code: String) {
  override def toString = code
}

object Language {
  def apply(name: String, code: String): Language = new Language(name, code)

  val en = Language("English", "en")
  val cs = Language("Czech", "cs")
}
