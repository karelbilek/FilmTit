package cz.filmtit.dataimport.alignment.model

import cz.filmtit.core.Configuration
import cz.filmtit.share.MediaSource
import java.io._
import cz.filmtit.share.parsing.UnprocessedParserSrt
import cz.filmtit.share.parsing.UnprocessedChunk
import scala.collection.JavaConversions._
import java.util.zip.GZIPInputStream
import cz.filmtit.share.Language
import scala.io.Codec
import java.nio.charset._

/**
 * A representation of .srt subtitle file
 *
 * @constructor create a new SubtitleFile (don't call the constructor though, call
 *             SubtitleFile.maybeNew)
 * @param filmID ID of film
 * @param file the file itself
 * @param language language of file
 */
class SubtitleFile(val filmID:String, val file:File, val c:Configuration, val presetLanguage:Option[Language]=None)  {

  lazy val language:Option[Language] = {
       if (presetLanguage == None) {
         
         val s = readTextFromChunks
         LanguageDetector.detect(s, c)       
       } else {
         presetLanguage
       }
  }

  def fileNumber():String = file.getName.replaceAll("\\.gz","");

  def copyToFolder(folder:File) {
     val newf = new File(folder, file.getName)
     val writer = new BufferedWriter( new FileWriter( newf));
     writer.write(readText)
     writer.close
  }

  /**
   * Read the text from the .gz subtitle file
   * @return The raw string of the file
   */
   def readText():String = {
       
       val fin = new FileInputStream(file)
       val gzis = new GZIPInputStream(fin)
       
       val res = try {
        try {
            val readWindows:String = readWithCodec(gzis,new Codec(java.nio.charset.Charset.forName("windows-1250")))
            if (readWindows.startsWith("ď»ż1")) {
                readWithCodec(gzis, Codec.UTF8)
            } else {
                readWindows
            }
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

    import cz.filmtit.share.exceptions.ParsingException
   /**
    * Read the chunks from the file, parse it
    * @return "raw" chunks from srt
    */
    def readChunks():Seq[UnprocessedChunk] = {
        //public List<UnprocessedChunk> parseUnprocessed(String text)
        val parser = new UnprocessedParserSrt
        try {
            parser.parseUnprocessed(readText())
        } catch {
            case e: ParsingException =>
                println("TOO MANY ERRORS. IGNORING.")
                Seq[UnprocessedChunk]()
        }
    }

    
    def readTextFromChunks():String = {
        //done by par, so it is faster, but not necesarilly in order
        val chunks = readChunks
        if (chunks.size==0) {
            ""
        } else {
            chunks.par.map{_.getText}.reduce{_+"\n"+_}
        }
     }
}

object SubtitleFile {

  /**
   * Creates a file, if it exists
   * @param conf configuration determining location of file
   * @param filmID movie ID
   * @param subname name of subtitle file (without .gz, just number)
   * @return None if file doesn't exist, otherwise the subtitle file
   */
    def maybeNew(conf:Configuration, filmID:String, subname:String, testOnExistence:Boolean, testLanguage:Option[Language]=None, putLanguage:Option[Language]=None):Option[SubtitleFile] = {
        if ((!testOnExistence) ||new File(conf.getSubtitleName(subname)).exists()) {
            val f = Some(new SubtitleFile(filmID, new File(conf.getSubtitleName(subname)),  conf, putLanguage))
            if (testOnExistence) {
                //file has to exist in this branch
                //but we check it for language
                val lan:Option[Language] = f.flatMap{_.language}
                val testres = if (testLanguage==None) {
                    lan.isDefined;
                } else {
                    lan == testLanguage;
                }
                if (testres) {f} else {None}
            } else {
                //do not test on existence
                f
            }
        } else {
            //file does not exist
            None
        }
    }

}
