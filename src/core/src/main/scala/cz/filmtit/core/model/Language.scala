package cz.filmtit.core.model

/**
 * @author Joachim Daiber
 *
 */

class Language(name: String, code: String) {
  override def toString = code
}

object Language {
  def apply(name: String, code: String): Language = new Language(name, code)

  val en = Language("English", "en")
  val cz = Language("Czech", "cz")
}
