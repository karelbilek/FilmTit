package cz.filmtit.dataimport.alignment.model

import eval.TMEvaluator
import cz.filmtit.share.Language
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
class Aligner(subtitleFileAlignment:SubtitleFileAlignment, chunkAlignment:ChunkAlignment, goodFilePairChooser:GoodFilePairChooser, conf:Configuration, l1:Language, l2:Language) {
    

  def alignFiles(mapping:SubtitleMapping, maxFiles:Int=0): Iterable[Pair[SubtitleFile, SubtitleFile]] = {
       println("start")
       var counter = 0
       
       val pairs = mapping.moviesWithSubsBothLangs.flatMap{
         filmname =>
           val files = mapping.getSubtitles(filmname);
           if (filmname==None){
             None
           } else {
             if (maxFiles ==0 || maxFiles < counter) {
                counter+=1
                println("aligning file<->file")
                println("size je : "+files.get.size)
                subtitleFileAlignment.alignFiles(files.get)
             } else {
                None
             }
           }
       }
       println("done")
       val goodPairs = goodFilePairChooser.choosePairs(pairs)
       println("I chose files "+goodPairs.size)
       goodPairs
  }

  /**
   * Dose the aligning itself and write it to files
   *
   * @param mapping mapping of movie ID to subtitle files
   */
   def align(mapping:SubtitleMapping, maxFiles:Int=0, printDifferentSentenceLengths:Boolean=false) {
      
       println("vstup do mapping")
       val goodPairs = alignFiles(mapping, maxFiles)
       println("Goodpairs bude "+goodPairs.size())
       goodPairs.foreach {
        pair=>
           println("another : "+pair._1.filmID)
           val chunks = chunkAlignment.alignChunks(pair._1.readChunks, pair._2.readChunks)
           val pw: java.io.PrintWriter = new java.io.PrintWriter(new java.io.File(conf.getDataFileName(pair._1.filmID)))
           chunks.foreach {

             chunkPair=>
                
                
                val processedChunk1:Seq[TimedChunk] = processChunk(chunkPair._1, 0, 0L, l1)
                val processedChunk2:Seq[TimedChunk] = processChunk(chunkPair._2, 0, 0L, l2)
                if (processedChunk1.length == processedChunk2.length) {
                   (0 to processedChunk1.length-1).foreach {
                     i => Writer.write(pw, processedChunk1(i), processedChunk2(i))
                   }
                } else {
                    //writes BOTH sentences as is
                   if (printDifferentSentenceLengths) {
                       Writer.write(pw,chunkPair._1, chunkPair._2);
                   }
                }
           }
           pw.close() 
       }
       
    }
}

object Aligner {

    

    def writeHeldoutData(alignment:SubtitleFileAlignment,choser:GoodFilePairChooser, mapping:SubtitleMapping, conf:Configuration, where:String) {
        
       val a = new Aligner(alignment, null, choser, conf, Language.EN, Language.CS);
       val alignedMovies = a.alignFiles(mapping).map{_._1.filmID}
       val nonalignedMovies = mapping.moviesWithSubs.toSet -- alignedMovies

       val nonalignedSubtitles = nonalignedMovies.map{m=>(m, mapping.getSubtitles(m).get.find{sf=>sf.language==Some(Language.EN)})}.filter{_._2.isDefined}
       val writer = new java.io.PrintWriter(new java.io.File(where))

        nonalignedSubtitles.foreach{
            case (movie, file)=>writer.println(movie+"\n"+file.get.fileNumber)
        }
        writer.close

    
    }

    def writeHeldoutData(where:String="heldout") {

           val c = new Configuration("configuration.xml")

           val mapping = new SubtitleMapping(c)
           val filename ="aligned" 
        
           val file = new java.io.File(filename)
           val map = TMEvaluator.loadFilePairsToMap(file, c)

           writeHeldoutData(new SubtitleFileAlignmentFromFile(Language.EN, Language.CS, map), new GoodFilePairChooserFromFile(map), mapping,c, where)
 

    }

    def main(args:Array[String]) = writeHeldoutData("heldout")

    def writeFilePairsToPrintWriter(pairs: Iterable[Pair[SubtitleFile, SubtitleFile]], writer:java.io.PrintWriter) {
         pairs.foreach {
            case Pair(sf1, sf2) => 
            writer.println(sf1.filmID+"\t"+sf1.fileNumber + "\t"+sf2.fileNumber)
            println(sf1.filmID+"\t"+sf1.fileNumber + "\t"+sf2.fileNumber)
         
         }
    }
  def writeFilePairsToFile(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]], where:java.io.File) = {
      val writer = new java.io.PrintWriter(where)
      writeFilePairsToPrintWriter(pairs, writer)
      writer.close
  }

  def readFilePairsFromFile(where:java.io.File, conf:Configuration, l1:Language, l2:Language, includeNonExistingFiles:Boolean) : Iterable[Pair[SubtitleFile, SubtitleFile]] = {
      val reg = """(.*)\t(.*)\t(.*)""".r
      io.Source.fromFile(where).getLines().toIterable.flatMap {
         case reg(movie, descrL, descrR) => 
            val sub1 = SubtitleFile.maybeNew(conf, movie, descrL, !includeNonExistingFiles, Some(l1)) 
            val sub2 = SubtitleFile.maybeNew(conf, movie, descrR, !includeNonExistingFiles, Some(l2)) 
            if (sub1.isDefined && sub2.isDefined) {
                Some((sub1.get, sub2.get))
            } else {
                None
            }
      }
  }




}
