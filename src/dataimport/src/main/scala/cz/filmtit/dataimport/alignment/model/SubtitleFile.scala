package cz.filmtit.dataimport.alignment.model

import java.io.File
import cz.filmtit.share.parsing.ParserSrt
import cz.filmtit.share.parsing.UnprocessedChunk
import scala.collection.JavaConversions._

class SubtitleFile(val source:MediaSource, val file:File)  {
    def readText():String = {
       val source = scala.io.Source.fromFile(file)
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
    
    def maybeNew(source: MediaSource, filename:String):Option[SubtitleFile] = {
        if (new File(filename).exists()) {
            Some(new SubtitleFile(source, new File(filename)))
        } else {
            None
        }
    }

}
