package cz.filmtit.core.io


import collection.mutable.HashMap
import cz.filmtit.core.io.load.IMDB
import org.json.JSONObject
import io.Source
import collection.mutable.HashSet
import cz.filmtit.core.factory.Factory
import cz.filmtit.core.model.data.{MediaSource, TranslationPair}
import cz.filmtit.core.model.TranslationMemory
import java.io.{IOException, IOError, File}


/**
 * @author Joachim Daiber
 */

object Import {

  var subtitles = HashMap[String, MediaSource]()

  def loadSubtitleMapping(mappingFile: File) {
    Source.fromFile(mappingFile).getLines() foreach
      { line =>
        val data = line.split("\t")

        if (!subtitles.contains(data(0)))
          subtitles.put(data(0),
            new MediaSource(
              data(7),
              data(8),
              new HashSet[String]()
            )
          )
      }
  }

  var hit = 0
  var miss = 0

  def loadMediaSource(id: String): MediaSource = subtitles.get(id) match {
    case Some(mediaSource) => MediaSource.fromIMDB(mediaSource.title, mediaSource.year)
    case None => throw new IOException("No movie found in the DB!")
  }


  def loadChunks(tm: TranslationMemory, folder: File) {

    tm.initialize(
      folder.listFiles flatMap (
        sourceFile => {
          println( "Processing file %s".format(sourceFile) )
          val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
          mediaSource.id = tm.addMediaSource(mediaSource)

          Source.fromFile(sourceFile).getLines()
            .map( TranslationPair.fromString(_) )
            .filter( _ != null )
            .map( { pair: TranslationPair => pair.mediaSource = mediaSource; pair } )
        }))
  }


  def main(args: Array[String]) {
    loadSubtitleMapping(new File(args(0)))
    println("Loaded subtitle -> movie mapping")

    val tm = Factory.createTM()
    loadChunks(tm, new File(args(1)))
    println("hits:" + hit + ", miss:" + miss)
  }

}