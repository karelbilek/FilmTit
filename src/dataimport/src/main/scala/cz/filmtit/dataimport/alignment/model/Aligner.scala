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

package cz.filmtit.dataimport.alignment.model

import eval.TMEvaluator
import cz.filmtit.share.Language
import cz.filmtit.share.TimedChunk
import cz.filmtit.core.Configuration
import scala.collection.JavaConversions._
import cz.filmtit.share.parsing.Parser.processChunk
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.io.{SubtitleFile, AlignedCorpusWriter}

//

 /**
  * An object for general alignment. I am using "English" and "Czech" in the doc even when
  * it can be more general.
  *
  * @constructor create a new aligner.
  * @param subtitleFileAlignment how to decide which file goes to which file?
  * @param chunkAlignment how to decide, which chunk goes which what chunk in a file?
  * @param goodFilePairChooser how to decide, which file pairs are good enough
  * @param l1 English
  * @param l2 Czech
  * @param conf Configuration that tells me where to write files
  * @author KB
  */
class Aligner(
               subtitleFileAlignment:SubtitleFileAlignment,
               chunkAlignment:ChunkAlignment,
               goodFilePairChooser:GoodFilePairChooser,
               conf:Configuration,
               l1:Language, l2:Language) {

  /**
   * Does the file-to-file alignment and filtering the "bad" ones.
   * @param mapping SubtitleMapping, that tells us movie ID -> subtitle file mapping
   * @return Pair of files; first is English, second is Czech
   */
  def alignFiles(mapping:SubtitleMapping): Iterable[Pair[SubtitleFile, SubtitleFile]] = {
       println("start")

       val pairs = mapping.moviesWithSubsBothLangs.flatMap{ movieID =>
           val files = mapping.getSubtitles(movieID)
           println("aligning file<->file")
           println("size is : "+files.size)
           subtitleFileAlignment.alignFiles(files)
       }
       println("done")
       val goodFilePairs = goodFilePairChooser.choosePairs(pairs)
       println("I chose files "+goodFilePairs.size)
       goodFilePairs
  }


   /**
    * Does ALL the three aligning steps and write the results to a folder,
    * that is defined in configuration.xml
    * @param mapping SubtitleMapping, that tells us movie ID -> subtitle file mapping
    * @param printDifferentSentenceLengths What should Aligner do when Czech and English
    *                                      has different number of sourceSentencesToFind. True = print as-is,
    *                                      false = ignore them.
    */
   def align(mapping:SubtitleMapping, printDifferentSentenceLengths:Boolean=false) {
      
       println("entering mapping")
       val goodFilePairs = alignFiles(mapping)
       println("goodFilePairs size "+goodFilePairs.size)
       goodFilePairs.foreach { pair=>
           println("another : "+pair._1.filmID)

           //align the chunks
           val goodChunkPairs = chunkAlignment.alignChunks(pair._1.readChunks, pair._2.readChunks)

           //open the file
           val printWriter: java.io.PrintWriter =
             new java.io.PrintWriter(new java.io.File(conf.getDataFileName(pair._1.filmID)))

           goodChunkPairs.foreach {
                          //EN                  CS
             case (unproccessedChunkL1, unprocessedChunkL2)=>
                
                //split chunks to sourceSentencesToFind
                val processedChunk1:Seq[TimedChunk] = processChunk(unproccessedChunkL1, 0, 0L, l1)
                val processedChunk2:Seq[TimedChunk] = processChunk(unprocessedChunkL2, 0, 0L, l2)

                if (processedChunk1.length == processedChunk2.length) {
                   (0 to processedChunk1.length-1).foreach {
                     i => AlignedCorpusWriter.write(printWriter, processedChunk1(i), processedChunk2(i))
                   }
                } else {
                    //writes BOTH sourceSentencesToFind as is
                   if (printDifferentSentenceLengths) {
                       AlignedCorpusWriter.write(printWriter,unproccessedChunkL1, unprocessedChunkL2)
                   }
                }
           }
           printWriter.close()
       }
       
    }
}
