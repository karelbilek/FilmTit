package cz.filmtit.core.model.data

import collection.mutable.HashSet

/**
 * @author Joachim Daiber
 *
 */

class MediaSource(val title: String, val year: String, var genres: HashSet[String]) {

  var id: Long = _

  def this(title: String, year: String, genres: String) {
    this(title, year, HashSet() ++ genres.split(",[ ]*"))
  }

}

