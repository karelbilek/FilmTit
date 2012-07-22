package cz.filmtit.core.model


/**
 * @author Joachim Daiber
 *
 */

object Patterns {
  val number = "^[0-9]+".r

  val punctuation = "[\\p{Punct}]".r

  val stopwordPath = "/Users/jodaiber/Desktop/stopwords/"
  //var stopwords: HashMap[Language, HashSet[String]] = HashMap()
  //Language.values foreach {
  //  language =>
  //    stopwords.put(language, HashSet() ++ Source.fromFile(stopwordPath + language + ".txt").getLines().map(_.toLowerCase) )
  //}

  //def isStopWord(token: String, language: Language): Boolean = stopwords(language) contains token.toLowerCase
}



