package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.TimedChunk
import cz.filmtit.core.Configuration
import scala.collection.JavaConversions._
import cz.filmtit.share.parsing.Parser.processChunk
import cz.filmtit.dataimport.SubtitleMapping

/** An object for general alignmenr.
  *
  * @constructor create a new aligner.
  * @param subtitleFileAlignment how to decide which file goes to which file?
  * @param chunkAlignment how to decide, which chunk goes which what chunk in a file?
  * @param goodFilePairChooser how to decide, which file pairs are good enough
  * @param conf Configuration that tells me where to write files
  */
class Aligner(subtitleFileAlignment:SubtitleFileAlignment, chunkAlignment:ChunkAlignment, goodFilePairChooser:GoodFilePairChooser, conf:Configuration) {
    
    val writer = new Writer(conf)

  /**
   * Dose the aligning itself and write it to files
   *
   * @param mapping mapping of movie ID to subtitle files
   */
   def align(mapping:SubtitleMapping) {
       
       println("start")
       val pairs = mapping.subtitles.keys.flatMap{
         filmname =>
           val files = mapping.getSubtitles(filmname);
           if (filmname==None){
             None
           } else {
             println("aligning file<->file")
             subtitleFileAlignment.alignFiles(files.get)
           }
       }
       println("done")
       val goodPairs = goodFilePairChooser.choosePairs(pairs)
       goodPairs.foreach {
        pair=>
           val chunks = chunkAlignment.alignChunks(pair._1.readChunks, pair._2.readChunks)
           chunks.foreach {
             chunkPair=>
                val processedChunk1:Seq[TimedChunk] = processChunk(chunkPair._1, 0, 0L)
                val processedChunk2:Seq[TimedChunk] = processChunk(chunkPair._2, 0, 0L)
                if (processedChunk1.length == processedChunk2.length) {
                   (0 to processedChunk1.length-1).foreach {
                     i => writer.write(pair._1.filmID, processedChunk1(i), processedChunk2(i))
                   }
                } else {
                   writer.write(pair._1.filmID,chunkPair._1, chunkPair._2);
                }
           }
           writer.flush(pair._1.filmID)
       }
       
    }
}
