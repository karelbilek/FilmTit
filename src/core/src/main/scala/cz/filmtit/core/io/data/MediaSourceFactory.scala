package cz.filmtit.core.io.data

import java.util.LinkedList
import org.json.JSONObject
import collection.mutable.HashMap
import cz.filmtit.share.MediaSource

object MediaSourceFactory {

  def fromIMDB(title: String, year: String): MediaSource = {
    try {
      val imdbInfo: JSONObject = IMDB.queryFirstBest(title, year)
      new MediaSource(title, year, imdbInfo.getString("Genre"))
    } catch {
      case e: Exception => new MediaSource(title, year)
    }
  }


  def suggestionsFromIMDB(
    title: String,
    year: String
    ): java.util.List[MediaSource] = {
    try {
      val nbest = IMDB.queryNBest(title, year)
      val l = new LinkedList[MediaSource]()
      nbest foreach {jo: JSONObject => l.add(new MediaSource(title, year, jo.getString("Genre")))}
      l
    } catch {
      case e: Exception => {
        val l = new LinkedList[MediaSource]()
        l.add(new MediaSource(title, year))
        l
      }
    }
  }


  def fromCachedIMDB(
    title: String,
    year: String,
    cache: HashMap[String, MediaSource]
    ): MediaSource = {
    if (cache != null) {
      cache.get((title, year).toString()) match {
        case Some(ms) => {
          ms.setId(null)
          ms
        }
        case None => {
          val ms = fromIMDB(title, year)
          cache.put((title, year).toString(), ms)
          ms
        }
      }
    } else {
      fromIMDB(title, year)
    }
  }

}

