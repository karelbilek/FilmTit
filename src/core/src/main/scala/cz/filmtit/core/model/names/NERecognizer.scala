package cz.filmtit.core.model.names

import cz.filmtit.core.model.annotation.ChunkAnnotation
import cz.filmtit.core.model.data.Chunk


/**
 * @author Joachim Daiber
 *
 *
 *
 */

abstract class NERecognizer(neClass: ChunkAnnotation) {

  def detect(chunk: Chunk): Chunk

}
