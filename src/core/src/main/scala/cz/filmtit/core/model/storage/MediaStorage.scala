package cz.filmtit.core.model.storage

import cz.filmtit.core.model.data.MediaSource

/**
 * @author Joachim Daiber
 *
 *
 *
 */

trait MediaStorage {

  def reset()
  def addMediaSource(mediaSource: MediaSource): Long
  def getMediaSource(id: Int): MediaSource

}
