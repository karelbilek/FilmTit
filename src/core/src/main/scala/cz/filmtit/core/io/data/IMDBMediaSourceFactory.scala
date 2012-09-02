package cz.filmtit.core.io.data

import _root_.java.util
import java.net.URLEncoder
import cz.filmtit.core.model.MediaSourceFactory
import org.json.JSONObject
import io.Source
import cz.filmtit.share.MediaSource

/**
 * A [[cz.filmtit.core.model.MediaSourceFactory]] based on data from Freebase.com.
 *
 * @deprecated This is deprecated since the IMDB API service was stopped.
 *
 * @author Joachim Daiber
 */

class IMDBMediaSourceFactory extends MediaSourceFactory {

  def getSuggestion(title: String, year: String): MediaSource = getSuggestions(title, year).get(0)

  def getSuggestions(title: String, year: String): java.util.List[MediaSource] = {
    try {
      val imdbInfo: JSONObject = queryFirstBest(title, year)
      val l = new java.util.ArrayList[MediaSource]()
      l.add(new MediaSource(title, year, imdbInfo.getString("Genre")))
      l
    } catch {
      case e: Exception => java.util.Arrays.asList({new MediaSource(title, year)})
    }
  }

  def getSuggestions(title: String): java.util.List[MediaSource] = getSuggestions(title, "")

  private def queryFirstBest(title: String, year: String): JSONObject = {
    new JSONObject(queryAll(title, year).next())
  }

  private def queryAll(title: String, year: String): Iterator[String] = {
    val tvShowPattern = "\"(.+)\" .+".r

    val response = title match {
      case tvShowPattern(titleShow) => {
        Source.fromURL("http://www.imdbapi.com/?t=%s".format(
          URLEncoder.encode(titleShow, "utf-8"))).getLines()
      }
      case _ => {
        Source.fromURL("http://www.imdbapi.com/?t=%s&y=%s".format(
          URLEncoder.encode(title, "utf-8"), year)).getLines()
      }
    }
    response
  }

}