package cz.filmtit.core.model.data

import scala.collection.JavaConversions._
import cz.filmtit.share.annotations.AnnotationType
import cz.filmtit.share.Chunk

object ChunkUtils {
    

 
 def toAnnotatedString(
    chunk: Chunk,
    format: (AnnotationType, String) => String = 
        { (t, _) => "<" + t.getDescription + ">" }
  ): String = {
 
    val surfaceform = chunk.getSurfaceForm

    var lastOffset = 0
    (chunk.getAnnotations map {
      annotation => {
        val anType = annotation.getType
        val from = annotation.getBegin
        val to = annotation.getEnd
        
        "%s%s".format(
        surfaceform.substring(lastOffset, from),
        format(anType,
          surfaceform.substring(from, math.min(surfaceform.size, to))), {
          lastOffset = to
        }
        )
      }
    }).mkString + surfaceform.substring(math.min(surfaceform.size, lastOffset))

  }


}
