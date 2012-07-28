package cz.filmtit.userspace

import cz.filmtit.share._
import cz.filmtit.core.model.TranslationMemory

object ParallelHelper {

    def getTranslationsParallel(chunks:java.util.List[TimedChunk], session:Session, tm:TranslationMemory):java.util.List[TranslationResult] = {
        
        import scala.collection.JavaConversions._

        //trying to parallelize the search
        val r = chunks.par.map{chunk=>session.getTranslationResults(chunk, tm)}.seq
        
        val l = new java.util.ArrayList[TranslationResult]()
        l.addAll(r)
        l
    
    }

}
