package cz.filmtit.core.model.data

import org.json.JSONObject
import cz.filmtit.core.io.data.IMDB
import collection.mutable.HashMap
import cz.filmtit.share.MediaSource

object MediaSourceFactory {

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
        case Some(ms) => {
          ms.setId(null)
          ms
        }
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

