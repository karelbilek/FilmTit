package cz.filmtit.core.io

import java.io.File
import scala.io.Source
import cz.filmtit.core.model.{MediaSource, TranslationPairStorage, TranslationPair}
import com.github.savvasdalkitsis.jtmdb.{GeneralSettings, Movie}
import com.github.savvasdalkitsis.jtmdb.Movie.deepSearch
import collection.JavaConversions._
import collection.mutable.HashMap
import java.util.{Calendar, GregorianCalendar}
import java.text.SimpleDateFormat
import collection.mutable.HashSet

/**
 * @author Joachim Daiber
 */

object AlignedChunkLoader {

  GeneralSettings.setApiKey("")
  var subtitles = HashMap[String, MediaSource]()

  def loadSubtitleMapping() {
    Source.fromFile("/Users/jodaiber/Dropbox/FilmTit/Examples, experiments/opensubtitles stats/export_final.txt").getLines() foreach
      { line =>
        val data = line.split("\t")

        if (!subtitles.contains(data(0)))
          subtitles.put(data(0), new MediaSource(data(7), data(8).toInt, null))
      }
  }

  var hit = 0
  var miss = 0

  def loadMediaSource(id: String): MediaSource = {
    subtitles.get(id) match {
      case Some(mediaSource) =>
      {
        deepSearch(mediaSource.title) find (movie => {
          //Get the first movie with a matching year.
          movie.getReleasedDate != null &&
          new SimpleDateFormat("yyyy").format(movie.getReleasedDate).toInt == mediaSource.year
        }) match {
          case Some(movie) => {
            hit += 1
            mediaSource.genres = movie.getGenres.map(_.getName).asInstanceOf[HashSet[String]]
          }
          case None => miss += 1
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

    //storage.initialize()

    folder.listFiles foreach (
      sourceFile => {
        val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
        Source.fromFile(sourceFile).getLines().asInstanceOf[Iterator[TranslationPair]].map(_.mediaSource = mediaSource)
      })

  }

  def main(args: Array[String]) {
    loadSubtitleMapping()
    loadChunks(null, new File("/Users/jodaiber/Desktop/LCT/LCT W11:12/FilmTit/data/parallel"))
    println("hits:" + hit + ", miss:" + miss)
  }

}