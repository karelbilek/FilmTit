package cz.filmtit.core.model

import cz.filmtit.share.MediaSource

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
  def getSuggestions(title: String): java.util.List[MediaSource]

}
