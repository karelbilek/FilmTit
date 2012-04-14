package cz.filmtit.core.model.data

import org.json.JSONObject
import cz.filmtit.core.io.data.IMDB
import collection.mutable.{HashMap, HashSet}


/**
 * The source of a subtitle chunk. This may be a movie, TV series etc.
 *
 * @author Joachim Daiber
 */

class MediaSource(val title: String, val year: String, var genres: HashSet[String]) extends Serializable{

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
    try {
      val imdbInfo: JSONObject = IMDB.query(title, year)
      new MediaSource(title, year, imdbInfo.getString("Genre"))
    } catch {
      case e: Exception => new MediaSource(title, year)
    }
  }

  def fromCachedIMDB(title: String, year: String, cache: HashMap[String, MediaSource]): MediaSource = {
    if (cache != null) {
      cache.get( (title, year).toString() ) match {
        case Some(ms) => ms
        case None => {
          val ms = fromIMDB(title, year)
          cache.put( (title, year).toString(), ms)
          ms
        }
      }
    } else {
      fromIMDB(title, year)
    }
  }

}

