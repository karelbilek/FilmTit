package cz.filmtit.core.model

import collection.mutable.HashMap

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
  val fromCode = HashMap[String, Language]()

  def apply(name: String, code: String): Language = {
    val lang = new Language(name, code)
    fromCode.put(code, lang)
    lang
  }

  val en = Language("English", "en")
  val cs = Language("Czech", "cs")
}
