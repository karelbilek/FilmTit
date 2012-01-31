package cz.filmtit.core.io

import cz.filmtit.core.model.{MediaSource, TranslationPairStorage, TranslationPair}

import collection.mutable.HashMap
import org.json.JSONObject
import java.net.URL
import java.io.{BufferedInputStream, File}
import io.{BufferedSource, Source}
import cz.filmtit.core.database.PostgresFirstLetterStorage
import collection.mutable.HashSet
import java.net.URLEncoder
import cz.filmtit.core.model.TranslationPair.fromString
import cz.filmtit.core.model.Language


/**
 * @author Joachim Daiber
 */

object AlignedChunkLoader {

  var subtitles = HashMap[String, MediaSource]()

  def loadSubtitleMapping() {
    Source.fromFile("/Users/jodaiber/Dropbox/FilmTit/Examples, experiments/opensubtitles stats/export_final.txt").getLines() foreach
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


  def loadChunks(storage: TranslationPairStorage, folder: File) {

    storage.initialize(
      folder.listFiles flatMap (
        sourceFile => {
          val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
          mediaSource.id = storage.addMediaSource(mediaSource)

          Source.fromFile(sourceFile).getLines()
            .map( TranslationPair.fromString(_) )
            .filter( _ != null )
            .map( { pair: TranslationPair => pair.mediaSource = mediaSource; pair } )
        })
    )

  }

  def main(args: Array[String]) {
    loadSubtitleMapping()
    val storage: PostgresFirstLetterStorage = new PostgresFirstLetterStorage(Language.en, Language.cz)

    loadChunks(storage, new File("/Users/jodaiber/Desktop/LCT/LCT W11:12/FilmTit/data/parallel/utf8"))
    println("hits:" + hit + ", miss:" + miss)

  }

  def queryIMDB(title: String, year: String): JSONObject = {
    val patternTVShow = "\"(.+)\" .+".r

    val response = title match {
      case patternTVShow(titleShow) => {
        Source.fromURL( "http://www.imdbapi.com/?t=%s".format(URLEncoder.encode(titleShow, "utf-8")) ).getLines()
      }
      case _ => {
        Source.fromURL( "http://www.imdbapi.com/?t=%s&y=%s".format(URLEncoder.encode(title, "utf-8"), year) ).getLines()
      }
    }

    new JSONObject( response.next() )
  }

}