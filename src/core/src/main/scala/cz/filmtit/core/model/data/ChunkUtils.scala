package cz.filmtit.core.model.data

import scala.collection.JavaConversions._
import cz.filmtit.share.annotations.AnnotationType
import cz.filmtit.share.Chunk

class ChunkUtils(chunk:Chunk) {
    

 val surfaceform = chunk.getSurfaceForm
 
 def toAnnotatedString(
    format: (AnnotationType, String) => String = 
        { (t, _) => "<" + t.getDescription + ">" }
  ): String = {

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

object ChunkUtils { 
    implicit def utilWrapper(chunk:Chunk) = new ChunkUtils(chunk)
}
