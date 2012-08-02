package cz.filmtit.core.io.data

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
 * @author Joachim Daiber
 */

class FreebaseMediaSourceFactory(val apiKey: String, val n: Int = 10) extends MediaSourceFactory {

  def this() {
    this(null)
  }

  def urlApiKey = if (apiKey != null) "&key=" + apiKey else ""

  val tvShowPattern = "\"(.+)\" .+".r
  implicit val formats = net.liftweb.json.DefaultFormats


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
      case e: Exception => fbDate
    }
  }

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
    new ArrayList((getMoviesByTitle(title) \ "result").children.map(getMediaSource).flatten)
  }

  def getMediaSource(jsonObject: JValue): Option[MediaSource] = {
    if ((jsonObject \ "notable" \ "id").equals(new JString("/film/film"))) {
      mediaSourceFromFilm((jsonObject \ "mid").extract[String])
    } else if ((jsonObject \ "notable" \ "id").equals(new JString("/tv/tv_program"))) {
      mediaSourceFromTVShow((jsonObject \ "mid").extract[String])
    } else {
      None
    }
  }

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
