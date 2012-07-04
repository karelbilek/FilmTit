package cz.filmtit.dataimport

import io.Source
import cz.filmtit.share.MediaSource
import collection.mutable.ListBuffer
import collection.mutable.HashMap
import alignment.model.SubtitleFile
import java.io.File
import cz.filmtit.share.Language
import cz.filmtit.core.Configuration

/**
 * Object that holds all the information from export.txt file
 * basically mapping both from movie ID to movie as mediasource
 * but also mapping from movie ID to list of subtitle files
 *
 * @param conf configuration, determining where is the file
 */
class SubtitleMapping(val conf:Configuration) {
   val subtitles = HashMap[String, Pair[MediaSource, ListBuffer[SubtitleFile]]]()

  /**
   * Loads the index file that contains the
   * mapping from subtitle files to movies.
   *
   * @param mappingFile index file
   */
  /*def load() =*/ {
    val mappingFile = conf.fileMediasourceMapping
    println("mapping file: "+mappingFile);
    Source.fromFile(mappingFile).getLines() foreach
      { line =>
        val data = line.split("\t")
        val filmID = data(0)

        val mediasource = new MediaSource(
              data(7),
              data(8),
              "");

        val language = if (data(2) == "eng") {Language.EN} else {Language.CS}


        val subtitlefile = SubtitleFile.maybeNew(conf, filmID, data(1), language, true)


        if (!subtitles.contains(filmID)) {
         if (subtitlefile.isDefined) {
             subtitles.put(filmID,(mediasource, ListBuffer(subtitlefile.get)))
             
          } else {
             subtitles.put(filmID,(mediasource, ListBuffer[SubtitleFile]()))
          }
        } else {
          if (subtitlefile.isDefined) {
             subtitles.get(filmID).get._2 += subtitlefile.get
          }
 
        }
      
      }
  }

  def getMediaSource(name:String):Option[MediaSource] = {
    if (subtitles.get(name).isDefined) {
        Some(subtitles.get(name).get._1)
    } else {
        None
    }
  }

  def getSubtitles(name:String):Option[List[SubtitleFile]] = {
    if (subtitles.get(name).isDefined) {
        Some(subtitles.get(name).get._2.toList)
    } else {
        None
    }
  }

  def hasSubtitles(name:String) = {
     val subs = getSubtitles(name)
     if (subs==None) {
        false;
     } else {
        if (subs.get.isEmpty) {
            false
        } else {
            true
        }
     }
  }
 
  def movies():Iterable[String] = subtitles.keys
  
  def moviesWithSubs():Iterable[String] = movies.filter{hasSubtitles(_)}

}
