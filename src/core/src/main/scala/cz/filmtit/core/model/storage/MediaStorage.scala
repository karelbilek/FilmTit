package cz.filmtit.core.model.storage

import cz.filmtit.core.model.data.MediaSource

/**
 * Interface for retrieving information about the movie/TV show that contained
 * the translation pair.
 *
 * @author Joachim Daiber
 */

trait MediaStorage {

  /**
   * Recreate all relevant database tables.
   */
  def reset()

  /**
   * Add a media source to the database.
   * @param mediaSource filled media source object
   * @return
   */
  def addMediaSource(mediaSource: MediaSource): Long


  /**
   * Get a media source by its database identifier.
   * @param id media source identifier
   * @return
   */
  def getMediaSource(id: Int): MediaSource

}
