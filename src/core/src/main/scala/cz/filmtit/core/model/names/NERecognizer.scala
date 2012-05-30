package cz.filmtit.core.model.names

import cz.filmtit.share.Chunk
import cz.filmtit.share.annotations.AnnotationType


/**
 * Interface for named entity recognizers. NE should be added as annotations
 * to the [[cz.filmtit.core.model.data.AnnotatedChunk]].
 *
 * @author Joachim Daiber
 */

abstract class NERecognizer(val neClass: AnnotationType) {


  /**
   * Recognize named entities and add them as annotations to the chunk.
   *
   * @param chunk chunk on which NER will be performed
   */

  def detect(chunk: Chunk)

}
