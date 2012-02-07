package cz.filmtit.core.io.load

import org.json.JSONObject
import io.Source
import java.net.URLEncoder


/**
 * @author Joachim Daiber
 *
 *
 *
 */

object IMDB {

  def query(title: String, year: String): JSONObject = {
    val patternTVShow = "\"(.+)\" .+".r

    val response = title match {
      case patternTVShow(titleShow) => {
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
