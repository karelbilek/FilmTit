package cz.filmtit.dataimport.alignment.model.eval

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.core.Configuration
import cz.filmtit.share._
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.aligners.levensthein._

class LevAlignmentHistogram(val fileToWrite:String, val tolerance:Int) {
    
    def ohodnot(){

        val c = new Configuration("configuration.xml")
        val l1 = Language.EN
        val l2 = Language.CS
        
        val mapping = new SubtitleMapping(c, true)
        
        val counter = new LevenstheinDistanceCounter(tolerance)

        val fileAlignment = new LevenstheinSubtitleFileAlignment(Language.EN, Language.CS, counter)

        println("mapdan")
        val pairs = mapping.moviesWithSubsBothLangs.flatMap{
         filmname =>
           val files = mapping.getSubtitles(filmname)
           if (filmname==None){
                None
           } else {
                println("aligning file<->file")
                println("size je : "+files.get.size)
                fileAlignment.alignFiles(files.get)
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


object LevAlignmentHistogram {
    def main(args:Array[String]) {
        val hist = new LevAlignmentHistogram(args(0), args(1).toInt);
        hist.ohodnot()
    }
}
