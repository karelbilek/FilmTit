package cz.filmtit.core.model.names

import cz.filmtit.core.model.annotation.ChunkAnnotation
import cz.filmtit.core.model.data.Chunk


/**
 * Interface for named entity recognizers. NE should be added as annotations
 * to the [[cz.filmtit.core.model.data.Chunk]].
 *
 * @author Joachim Daiber
 */

abstract class NERecognizer(val neClass: ChunkAnnotation) {


  /**
   * Recognize named entities and add them as annotations to the chunk.
   *
   * @param chunk chunk on which NER will be performed
   */

  def detect(chunk: Chunk)

}
