package cz.filmtit.dataimport.alignment.model

import cz.filmtit.core.Configuration
import cz.filmtit.share.MediaSource
import java.io.File
import java.io.FileInputStream
import cz.filmtit.share.parsing.ParserSrt
import cz.filmtit.share.parsing.UnprocessedChunk
import scala.collection.JavaConversions._
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import cz.filmtit.share.Language
import scala.io.Codec
import java.nio.charset._
import java.io.IOException

/**
 * A representation of .srt subtitle file
 *
 * @constructor create a new SubtitleFile (don't call the constructor though, call
 *             SubtitleFile.maybeNew)
 * @param filmID ID of film
 * @param file the file itself
 * @param language language of file
 */
class SubtitleFile(val filmID:String, val file:File, val language:Language)  {

  def fileNumber():String = file.getName.replaceAll("\\.gz","");

  /**
   * Read the text from the .gz subtitle file
   * @return The raw string of the file
   */
   def readText():String = {
       
       val fin = new FileInputStream(file)
       val gzis = new GZIPInputStream(fin)
       
       val res = try {
        try {
            readWithCodec(gzis,new Codec(java.nio.charset.Charset.forName("windows-1250")))
        } catch {
         case e: UnmappableCharacterException => readWithCodec(gzis, Codec.UTF8)
        }
       } catch {
        case e:IOException => ""; 
        
       }
       fin.close();
      
      res
   }

    private def readWithCodec(stream:GZIPInputStream, codec:Codec):String = {
       val source = scala.io.Source.fromInputStream(stream)(codec)
       val buf = new StringBuilder
       for (line <- source.getLines()) {
          buf.append( line)
          buf.append( "\n")
       }
       
       
       source.close()
       buf.toString
     }

  /**
   * Read the chunks from the file, parse it
   * @return "raw" chunks from srt
   */
    def readChunks():Seq[UnprocessedChunk] = {
        //public List<UnprocessedChunk> parseUnprocessed(String text)
        val parser = new ParserSrt
        parser.parseUnprocessed(readText())
    }
}

object SubtitleFile {

  /**
   * Creates a file, if it exists
   * @param conf configuration determining location of file
   * @param filmID movie ID
   * @param subname name of subtitle file (without .gz, just number)
   * @param language language of subtitle
   * @return None if file doesn't exist, otherwise the subtitle file
   */
    def maybeNew(conf:Configuration, filmID:String, subname:String, language:Language, testOnExistence:Boolean):Option[SubtitleFile] = {
        if ((!testOnExistence) ||new File(conf.getSubtitleName(subname)).exists()) {
            Some(new SubtitleFile(filmID, new File(conf.getSubtitleName(subname)), language))
        } else {
            None
        }
    }

}
