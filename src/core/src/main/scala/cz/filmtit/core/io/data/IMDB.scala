package cz.filmtit.core.io.data

import org.json.JSONObject
import io.Source
import java.net.URLEncoder


/**
 * Utilities for retrieving movie/TV show information from IMDB.
 *
 * @author Joachim Daiber
 */

object IMDB {

  def query(title: String, year: String): JSONObject = {
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

    new JSONObject(response.next())
  }

}
