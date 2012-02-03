package cz.filmtit.core.model.names

import cz.filmtit.core.model.names.NEType.NEType
import cz.filmtit.core.model.Chunk


/**
 * @author Joachim Daiber
 *
 *
 *
 */

abstract class NERecognizer(neClass: NEType) {

  def detect(chunk: Chunk): Chunk

}
