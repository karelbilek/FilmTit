/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.core.io.data

import _root_.java.io.IOException
import javax.net.ssl.{TrustManager, X509TrustManager, HttpsURLConnection, SSLContext}
import javax.security.cert
import java.security.cert.X509Certificate
import cz.filmtit.share.MediaSource
import io.Source
import java.net.URLEncoder
import scala.collection.JavaConversions._
import net.liftweb.json.JsonAST._
import net.liftweb.json.{MappingException, JsonParser}
import cz.filmtit.core.model.MediaSourceFactory
import java.util.ArrayList
import scala.Some

/**
 * A [[cz.filmtit.core.model.MediaSourceFactory]] based on data from Freebase.com.
 *
 * @author Joachim Daiber
 */

class FreebaseMediaSourceFactory(val apiKey: String, val n: Int = 10) extends MediaSourceFactory {

  //Allow Freebase to be queried without SSL negotation
  FreebaseMediaSourceFactory.allowSSLCertificate()

  def this() {
    this(null)
  }

  def urlApiKey = if (apiKey != null && !apiKey.equals("")) "&key=" + apiKey else ""

  /**
   * Regular expression pattern to identify TV Shows, which are in the format
   *
   * "Scrubs" My first kill
   *
   * This may have to be adapted to new input data.
   */
  val tvShowPattern = "\"(.+)\" .+".r
  implicit val formats = net.liftweb.json.DefaultFormats

  /**
   * Query the Freebase API.
   *
   * @param query the query
   * @return a JSON object with the returned data
   */
  def query(query: String): JValue = {
    val freebaseData = Source.fromURL(
      "https://api.freebase.com/api/service/mqlread?query=%s%s".format(URLEncoder.encode(query, "utf-8"), urlApiKey)
    ).getLines().mkString("")

    JsonParser.parse(freebaseData)
  }

  val fbDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
  val yearFormat = new java.text.SimpleDateFormat("yyyy")


  def yearFromFB(fbDate: String): String = {
    try {
      yearFormat.format(fbDateFormat.parse(fbDate))
    } catch {
      case e: Exception => ""
    }
  }

  /**
   * Retrieve more information abount a Freebase entity of type /film/film
   *
   * @param mid identifier of the entitiy
   * @return
   */
  def mediaSourceFromFilm(mid: String): Option[MediaSource] = {
    val result = query(
      """ {"query":
           {
             "mid": "%s",
             "name": null,
             "/film/film/initial_release_date": null,
             "/film/film/genre": [],
             "/common/topic/image": [{
                 "id": null,
                 "optional": true
               }]
            }
        } """.format(mid)
    )

    val ms = new MediaSource()

    try {
      ms.setTitle((result \ "result" \ "name").extract[String])
    } catch {
      case e: MappingException => return None
    }

    try {
      ms.setYear(yearFromFB((result \ "result" \ "/film/film/initial_release_date").extract[String]))
    } catch {
      case e: Exception =>
    }

    try {
      val genres: List[String] = (result \ "result" \ "/film/film/genre").extract[List[String]]
      ms.setGenresString(genres.mkString(","))
    } catch {
      case e: MappingException =>
    }

    try {
      ms.setThumbnailURL("https://usercontent.googleapis.com/freebase/v1/image/%s?maxwidth=50&maxheight=50&pad=false".format((result \ "result" \ "/common/topic/image" \ "id").extract[String]))
    } catch {
      case e: MappingException =>
    }

    Some(ms)
  }


  /**
   * Retrieve more information abount a Freebase entity of type /tv/tv_programme
   *
   * @param mid identifier of the entitiy
   * @return
   */
  def mediaSourceFromTVShow(mid: String): Option[MediaSource] = {
    val result = query(
      """ {"query":
           {
             "mid": "%s",
             "name": null,
             "/tv/tv_program/air_date_of_first_episode": null,
             "/tv/tv_program/genre": [],
             "/common/topic/image": [{
                 "id": null,
                 "optional": true
               }]
            }
        } """.format(mid)
    )


    val ms = new MediaSource()

    try {
      ms.setTitle((result \ "result" \ "name").extract[String])
    } catch {
      case e: MappingException => return None
    }

    try {
      ms.setYear(yearFromFB((result \ "result" \ "/tv/tv_program/air_date_of_first_episode").extract[String]))
    } catch {
      case e: MappingException =>
    }

    try {
      val genres: List[String] = (result \ "result" \ "/tv/tv_program/genre").extract[List[String]]
      ms.setGenresString(genres.mkString(","))
    } catch {
      case e: MappingException =>
    }

    try {
      ms.setThumbnailURL("https://usercontent.googleapis.com/freebase/v1/image/%s?maxwidth=50&maxheight=50&pad=false".format((result \ "result" \ "/common/topic/image" \ "id").extract[String]))
    } catch {
      case e: MappingException =>
    }

    Some(ms)
  }

  def getSuggestion(title: String, year: String): MediaSource = {
    var firstBest: MediaSource = null

    try {
      for (result <- (getMoviesByTitle(title) \ "result").children) {
        getMediaSource(result) match {
          case Some(ms) => {
            if (ms.getYear != null && ms.getYear.equals(year))
              return ms
            else if (firstBest == null)
              firstBest = ms
          }
          case _ =>
        }
      }
    } catch {
      case e: IOException => firstBest = null
    }

    if (firstBest != null)
      firstBest
    else
      new MediaSource(title, year)
  }

  def getSuggestions(title: String, year: String): java.util.List[MediaSource] = {
    val suggestions = getSuggestions(title)
    suggestions.filter(_.getYear.equals(year))
  }

  def getSuggestions(title: String): java.util.List[MediaSource] = {
    val moviesFromFreebase = try {
       new ArrayList((getMoviesByTitle(title) \ "result").children.map(getMediaSource).flatten)
    } catch {
      case e: IOException => new ArrayList[MediaSource]()
    }

    val defaultMS = new MediaSource()
    defaultMS.setTitle(title)
    moviesFromFreebase.add(defaultMS)

    moviesFromFreebase
  }

  /**
   * Retrieve a MediaSource for the suggestion in JSON format.
   *
   * @param jsonObject JSON representation of the suggestion
   * @return
   */
  def getMediaSource(jsonObject: JValue): Option[MediaSource] = {
    if ((jsonObject \ "notable" \ "id").equals(new JString("/film/film"))) {
      mediaSourceFromFilm((jsonObject \ "mid").extract[String])
    } else if ((jsonObject \ "notable" \ "id").equals(new JString("/tv/tv_program"))) {
      mediaSourceFromTVShow((jsonObject \ "mid").extract[String])
    } else {
      None
    }
  }

  /**
   * Retrieve information about a media source by its title.
   *
   * @param title the title of the movie/TV show
   * @return
   */
  def getMoviesByTitle(title: String): JValue = {
    val response = title match {
      case tvShowPattern(titleShow) => {
        Source.fromURL("https://www.googleapis.com/freebase/v1/search?query=%s&limit=%d&indent=true&filter=(any%%20type:/tv/tv_program)%s".format(
          URLEncoder.encode(titleShow, "utf-8"), n, urlApiKey)).getLines().mkString("")
      }
      case _ => {
        Source.fromURL("https://www.googleapis.com/freebase/v1/search?query=%s&limit=%d&indent=true&filter=(any%%20type:/tv/tv_program%%20type:/film/film)%s".format(
          URLEncoder.encode(title, "utf-8"), n, urlApiKey)).getLines().mkString("")
      }
    }

    JsonParser.parse(response)
  }

}

object FreebaseMediaSourceFactory {

  /**
   * Beware: This tells Java URL to trust any HTTPS certificate, use with caution!
   */
  def allowSSLCertificate() {
    val trustAllCerts = Array[TrustManager](
      new X509TrustManager() {
        def getAcceptedIssuers(): Array[X509Certificate] = null

        def checkClientTrusted(certs: Array[X509Certificate], authType: String) {
        }
        def checkServerTrusted(certs: Array[X509Certificate], authType: String) {
        }
      }
    )

    // Install the all-trusting trust manager
    try {
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new java.security.SecureRandom())
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
    } catch {
      case e: Exception =>
    }
  }

}