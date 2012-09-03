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

package cz.filmtit.dataimport.alignment.model.eval

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.core.Configuration
import cz.filmtit.share._
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.aligners.levensthein._
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * Helper class to create the data for histogram for levensthein distance
 * @param fileToWrite Where to write the distances
 * @param tolerance How big to take tolerance for the equality
 */
class LevAlignmentHistogram(val fileToWrite:String, val tolerance:Int) {
    
    def writeHistogram(){

        val c = new Configuration("configuration.xml")

        
        val mapping = new SubtitleMapping(c, true)
        
        val counter = new LevenstheinDistanceCounter(tolerance)

        val fileAlignment = new LevenstheinSubtitleFileAlignment(Language.EN, Language.CS, counter)

        println("mapdan")
        val pairs:Iterable[Pair[SubtitleFile, SubtitleFile]] = mapping.moviesWithSubsBothLangs.flatMap{
         filmname =>
           val files = mapping.getSubtitles(filmname)
           if (filmname.isEmpty){
                None
           } else {
                println("aligning file<->file")
                println("size je : "+files.size)
                fileAlignment.alignFiles(files)
           }
        }
        println("alignment done")
        
        val writer = new java.io.PrintWriter(new java.io.File(fileToWrite))        

        pairs.foreach{
           case (sf1:SubtitleFile, sf2:SubtitleFile) =>
                val toPrint:String = sf1.filmID+"\t"+counter.count(sf1, sf2)
                writer.println(toPrint);
                println(toPrint)
                
        }
        writer.close()



    }
}



