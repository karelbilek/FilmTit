package cz.filmtit.core.io

import java.io.File
import scala.io.Source
import cz.filmtit.core.model.{MediaSource, TranslationPairStorage, TranslationPair}
import com.github.savvasdalkitsis.jtmdb.{GeneralSettings, Movie}

/**
 * @author Joachim Daiber
 */

object AlignedChunkLoader {

  Source.fromFile("/Users/jodaiber/Dropbox/FilmTit/Examples, experiments/opensubtitles stats/export_final.txt")
    .getLines().map(line => {
      val data = line.split("\t")
      //(data(0), new MediaSource(data(7), data(8)))
    })

  def loadMediaSource(id: String): MediaSource = {
    GeneralSettings
    val m = Movie.search("Inception")
    println()
    null
  }

  def loadChunks(storage: TranslationPairStorage, folder: File) {

    storage.initialize()

    folder.listFiles foreach (
      sourceFile => {
        val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
        Source.fromFile(sourceFile).getLines().asInstanceOf[Iterator[TranslationPair]].map(_.mediaSource = mediaSource)
      })

  }

  def main(args: Array[String]) {
    loadMediaSource("123")
  }

}