package cz.filmtit.core.model.data

import collection.mutable.HashSet
import org.json.JSONObject
import cz.filmtit.core.io.data.IMDB


/**
 * The source of a subtitle chunk. This may be a movie, TV series etc.
 *
 * @author Joachim Daiber
 */

class MediaSource(val title: String, val year: String, var genres: HashSet[String]) {

  var id: Long = _

  def this(title: String, year: String, genres: String) {
    this(title, year, HashSet() ++ genres.split(",[ ]*"))
  }

  def this(title: String, year: String) {
    this(title, year, "")
  }

}


object MediaSource {

  def fromIMDB(title: String, year: String): MediaSource = {
    val imdbInfo: JSONObject = IMDB.query(title, year)

    if (imdbInfo.has("Genre"))
      new MediaSource(title, year, imdbInfo.getString("Genre"))
    else
      new MediaSource(title, year)
  }

}

