package cz.filmtit.core.model

import _root_.java.util
import cz.filmtit.share.MediaSource
import collection.mutable.HashMap

/**
 * @author Joachim Daiber
 */

abstract class MediaSourceFactory {


  /**
   * Get possible MediaSources for an uploaded subtitle file.
   *
   * @param title title of the movie/TV show
   * @return
   */
  def getSuggestion(title: String, year: String): MediaSource

  /**
   * Get possible MediaSources for an uploaded subtitle file.
   *
   * @param title title of the movie/TV show
   * @return
   */
  def getSuggestions(title: String): java.util.List[MediaSource]

  /**
   * Get possible MediaSources for an uploaded subtitle file by title+year.
   *
   * @param title title of the movie/TV show
   * @param year year in which the move was released or the TV show first aired
   * @return
   */
  def getSuggestions(title: String, year: String): java.util.List[MediaSource]

  /**
   * Get the best MediaSource from the cache if it was already queried or look it up
   * and put it into the cache.
   *
   * @param title title of the movie/TV show
   * @param year year in which the move was released or the TV show first aired
   * @param cache
   * @return
   */
  def getCachedSuggestion(title: String, year: String, cache: HashMap[String, MediaSource]): MediaSource = {
    if (cache != null) {
      cache.get((title, year).toString()) match {
        case Some(ms) => {
          ms.setId(null)
          ms
        }
        case None => {
          val ms = getSuggestion(title, year)
          cache.put((title, year).toString(), ms)
          ms
        }
      }
    } else {
      getSuggestion(title, year)
    }
  }


}
