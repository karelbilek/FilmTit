package cz.filmtit.core.io


import collection.mutable.HashMap
import org.json.JSONObject
import java.io.File
import io.Source
import collection.mutable.HashSet
import java.net.URLEncoder
import cz.filmtit.core.factory.TMFactory
import cz.filmtit.core.tm.BackoffTranslationMemory
import cz.filmtit.core.model.{TranslationMemory, MediaSource, TranslationPairStorage, TranslationPair}


/**
 * @author Joachim Daiber
 */

object ImportChunks {

  var subtitles = HashMap[String, MediaSource]()

  def loadSubtitleMapping(mappingFile: File) {
    Source.fromFile(mappingFile).getLines() foreach
      { line =>
        val data = line.split("\t")

        if (!subtitles.contains(data(0)))
          subtitles.put(data(0), new MediaSource(data(7), data(8), new HashSet[String]()))
      }
  }

  var hit = 0
  var miss = 0

  def loadMediaSource(id: String): MediaSource = {
    subtitles.get(id) match {
      case Some(mediaSource) =>
      {
        val imdbInfo: JSONObject = queryIMDB(mediaSource.title, mediaSource.year)

        if (imdbInfo.has("Genre")) {
          hit += 1
          imdbInfo.getString("Genre").split(", ") foreach { mediaSource.genres += _ }
        }else{
          miss += 1
        }

        mediaSource
      }
      case None => {
        println("No movie found in the DB!")
        null
      }
    }
  }


  def loadChunks(tm: TranslationMemory, folder: File) {

    tm.initialize(
      folder.listFiles flatMap (
        sourceFile => {
          val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
          mediaSource.id = tm.addMediaSource(mediaSource)

          Source.fromFile(sourceFile).getLines()
            .map( TranslationPair.fromString(_) )
            .filter( _ != null )
            .map( { pair: TranslationPair => pair.mediaSource = mediaSource; pair } )
        }))
  }


  def queryIMDB(title: String, year: String): JSONObject = {
    val patternTVShow = "\"(.+)\" .+".r

    val response = title match {
      case patternTVShow(titleShow) => {
        Source.fromURL( "http://www.imdbapi.com/?t=%s".format(
          URLEncoder.encode(titleShow, "utf-8")) ).getLines()
      }
      case _ => {
        Source.fromURL( "http://www.imdbapi.com/?t=%s&y=%s".format(
          URLEncoder.encode(title, "utf-8"), year) ).getLines()
      }
    }

    new JSONObject( response.next() )
  }


  def main(args: Array[String]) {
    loadSubtitleMapping(new File(args(0)))

    val tm = TMFactory.defaultTM()

    loadChunks(tm, new File(args(1)))

    println("hits:" + hit + ", miss:" + miss)

  }

}