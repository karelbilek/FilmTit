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

class SubtitleFile(val filmID:String, val file:File, val language:Language)  {
    def readText():String = {
       val fin = new FileInputStream(file)
       val gzis = new GZIPInputStream(fin)
       val source = scala.io.Source.fromInputStream(gzis)(new Codec(java.nio.charset.Charset.forName("windows-1250")))
       val lines = source.mkString
       source.close()
       lines
    }

    def readChunks():Seq[UnprocessedChunk] = {
        //public List<UnprocessedChunk> parseUnprocessed(String text)
        val parser = new ParserSrt
        parser.parseUnprocessed(readText())
    }
}

object SubtitleFile {
    
    def maybeNew(conf:Configuration, filmID:String, subname:String, language:Language):Option[SubtitleFile] = {
        if (new File(conf.getSubtitleName(subname)).exists()) {
            Some(new SubtitleFile(filmID, new File(conf.getSubtitleName(subname)), language))
        } else {
            None
        }
    }

}
