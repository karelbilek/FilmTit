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

package cz.filmtit.core.model

import cz.filmtit.share.MediaSource
import collection.mutable.HashMap

/**
 * Interface for factories for [[cz.filmtit.share.MediaSource]] objects.
 *
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
