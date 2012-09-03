/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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
