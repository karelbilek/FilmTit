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


class SubtitleFile(val source:MediaSource, val file:File, val language:Language)  {
    def readText():String = {
       val fin = new FileInputStream(file)
       val gzis = new GZIPInputStream(fin)
       val source = scala.io.Source.fromInputStream(gzis)
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
    
    def maybeNew(conf:Configuration, media: MediaSource, subname:String, language:Language):Option[SubtitleFile] = {
        if (new File(conf.getSubtitleName(subname)).exists()) {
            Some(new SubtitleFile(media, new File(conf.getSubtitleName(subname)), language))
        } else {
            None
        }
    }

}
