package cz.filmtit.core.io


import collection.mutable.HashMap
import io.Source
import collection.mutable.HashSet
import cz.filmtit.core.model.data.{MediaSource, TranslationPair}
import cz.filmtit.core.model.TranslationMemory
import scala.util.Random
import cz.filmtit.core.{Configuration, Factory}
import java.io.{PrintWriter, IOException, File}


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

    tm.reset()

    val heldoutWriter = new PrintWriter(Configuration.heldoutFile)

    System.err.println("Processing files:")
    folder.listFiles filter(_.getName.endsWith(".txt")) grouped(100) foreach(
      (files: Array[File])=> tm.add(
        files flatMap ( (sourceFile: File) => {

          val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
          mediaSource.id = tm.mediaStorage.addMediaSource(mediaSource)

          System.err.println( "- %s: %s, %s, %s"
            .format(sourceFile.getName, mediaSource.title, mediaSource.year,
              if (mediaSource.genres.size > 0)
                mediaSource.genres.toString()
              else
                "Could not retrieve additional information"
            )
          )

          //Read all pairs from the file and convert them to translation pairs
          val pairs = Source.fromFile(sourceFile).getLines()
            .map( TranslationPair.fromString(_) )
            .filter( _ != null )
            .map( { pair => pair.setMediaSource(mediaSource); pair } )

          //Exclude heldoutSize% of the data as heldout data
          val (training, heldout) =
            pairs.toList.partition(_ => !(Random.nextFloat() < Configuration.heldoutSize) )

          heldout.foreach( pair => heldoutWriter.println(pair.toExternalString) )

          training
        }))
      )

    heldoutWriter.close()
  }


  def main(args: Array[String]) {
    loadSubtitleMapping(new File(args(0)))
    System.err.println("Loaded subtitle -> movie mapping")

    val tm = Factory.createTM(readOnly = false)
    loadChunks(tm, new File(args(1)))
    //System.err.println("hits:" + hit + ", miss:" + miss)
  }

}