package cz.filmtit.core.model.storage

import cz.filmtit.core.model.data.MediaSource

/**
 * @author Joachim Daiber
 *
 *
 *
 */

trait MediaStorage {

  /**
   * Recreate all relevant database tables.
   */
  def reset()

  /**
   * Add a media source to the database.
   * @param mediaSource
   * @return
   */
  def addMediaSource(mediaSource: MediaSource): Long


  /**
   * Get a media source by its database identifier.
   * @param id
   * @return
   */
  def getMediaSource(id: Int): MediaSource

}
