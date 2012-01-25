package cz.filmtit.core.model

import cz.filmtit.core.model.Language._
import collection.mutable.{HashSet, HashMap}
import scala.io.Source

/**
 * @author Joachim Daiber
 *
 */

object Patterns {
  val number = "^[0-9]+".r

  val stopwordPath = "/Users/jodaiber/Desktop/stopwords/"
  var stopwords: HashMap[Language, HashSet[String]] = HashMap()
  Language.values foreach {
    language =>
      stopwords.put(language, HashSet() ++ Source.fromFile(stopwordPath + language + ".txt").getLines().map(_.toLowerCase) )
  }

  def isStopWord(token: String, language: Language): Boolean = stopwords(language) contains token.toLowerCase
}



