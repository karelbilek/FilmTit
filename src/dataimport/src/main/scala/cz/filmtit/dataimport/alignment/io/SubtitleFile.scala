package cz.filmtit.dataimport.alignment.io

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
import cz.filmtit.dataimport.alignment.model.LanguageDetector
import scala.Some

/**
 * A representation of .srt subtitle file. Apart from the address of the file,
 * it knows its language and ID of its mediasource.
 *
 * @constructor create a new SubtitleFile (don't call the constructor though, call
 *              SubtitleFile.fileIfExists)
 * @param filmID ID of film
 * @param file the file itself
 * @param presetLanguage suggested language of the file - if None, it is autodetected
 */
class SubtitleFile(val filmID: String,
                   file: File,
                   val c: Configuration,
                   presetLanguage: Option[Language] = None) {


  /**
   * Actual language of the file. Autodetected, if presetLanguage is None.
   */
  lazy val language: Option[Language] = {
    if (presetLanguage.isEmpty) {
      //parse, detect from parsed text
      val s = readTextFromChunks
      LanguageDetector.detect(s, c)
    } else {
      presetLanguage
    }
  }

  /**
   * Gets number from the .gz filename
   * @return
   */
  def fileNumber(): String = file.getName.replaceAll("\\.gz", "");

  /**
   * Copy itself to a different folder.
   * @param folder Folder where to copy.
   */
  def copyToFolder(folder: File) {
    val newf = new File(folder, file.getName)
    val writer = new BufferedWriter(new FileWriter(newf));
    writer.write(readText)
    writer.close
  }

  /**
   * Read the text from the .gz subtitle file
   * @return The raw string of the file
   */
  def readText(): String = {

    val fin = new FileInputStream(file)
    val gzis = new GZIPInputStream(fin)

    val res = try {
      try {
        val readWindows: String = readWithCodec(gzis, new Codec(java.nio.charset.Charset.forName("windows-1250")))
                              //this is BOM read with windows encoding
        if (readWindows.startsWith("ď»ż")) {
          readWithCodec(gzis, Codec.UTF8)  //BOM => read again with UTF8
        } else {
          readWindows
        }
      } catch {
        case e: UnmappableCharacterException => readWithCodec(gzis, Codec.UTF8)
      }
    } catch {
      case e: IOException => "";

    }
    fin.close();

    res
  }

  /**
   * Reads from GZipInputStream with a given codec
   * @param stream GZipInputStream of the file
   * @param codec Codec of the file
   * @return Raw string of the file
   */
  private def readWithCodec(stream: GZIPInputStream, codec: Codec): String = {
    val source = scala.io.Source.fromInputStream(stream)(codec)
    val buf = new StringBuilder
    for (line <- source.getLines()) {
      buf.append(line)
      buf.append("\n")
    }


    source.close()
    buf.toString
  }

  /**
   * Read the chunks from the file, parse it
   * @return "raw" chunks from srt
   */
  def readChunks(): Seq[UnprocessedChunk] = {
    //public List<UnprocessedChunk> parseUnprocessed(String text)
    val parser = new UnprocessedParserSrt
    try {
      parser.parseUnprocessed(readText())
    } catch {
      case e: cz.filmtit.share.exceptions.ParsingException =>
        println("TOO MANY ERRORS. IGNORING.")
        Seq[UnprocessedChunk]()
    }
  }

  /**
   * Adds a big string with all the text together, without the timing.
   * @return The text of the subtitle. It is returned in random order since
   *         we use it only for language detection, which works on trigram of letters anyway.
   */
  def readTextFromChunks(): String = {
    //done by par, so it is faster, but not necesarilly in order
    val chunks = readChunks
    if (chunks.size == 0) {
      ""
    } else {
      chunks.par.map {
        _.getText
      }.reduce {
        _ + "\n" + _
      }
    }
  }
}

object SubtitleFile {

  /**
   * Creates a file, if it exists
   * @param conf configuration determining location of file
   * @param filmID movie ID
   * @param subname mediaSourceID of subtitle file (without .gz, just number)
   * @return None if file doesn't exist, otherwise the subtitle file
   */
  def fileIfExists(
                    conf: Configuration,
                    filmID: String,
                    subname: String,
                    testOnExistence: Boolean,
                    testLanguage: Option[Language] = None,
                    putLanguage: Option[Language] = None): Option[SubtitleFile] = {
    if ((!testOnExistence) || new File(conf.getSubtitleName(subname)).exists()) {
      val f = Some(new SubtitleFile(filmID, new File(conf.getSubtitleName(subname)), conf, putLanguage))
      if (testOnExistence) {
        //file has to exist in this branch
        //but we check it for language
        val lan: Option[Language] = f.flatMap {
          _.language
        }
        val testres = if (testLanguage.isEmpty) {
          lan.isDefined;
        } else {
          lan == testLanguage;
        }
        if (testres) {
          f
        } else {
          None
        }
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
