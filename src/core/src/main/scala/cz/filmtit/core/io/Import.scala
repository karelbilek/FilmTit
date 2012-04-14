package cz.filmtit.core.io


import collection.mutable.HashMap
import io.Source
import collection.mutable.HashSet
import cz.filmtit.core.model.data.{MediaSource, TranslationPair}
import cz.filmtit.core.model.TranslationMemory
import scala.util.Random
import cz.filmtit.core.{Configuration, Factory}
import java.io._
import java.nio.charset.MalformedInputException


/**
 * @author Joachim Daiber
 */

object Import {

  /** Contains the subtitle file <-> media source mapping */
  var subtitles = HashMap[String, MediaSource]()

  /**
   * Loads the index file that contains the
   * mapping from subtitle files to movies.
   *
   * @param mappingFile index file
   */
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

  def writeIMDBCache() {
    System.err.println("Writing cached IMDB database to file...")
    new ObjectOutputStream(new FileOutputStream(Configuration.importIMDBCache)).writeObject(imdbCache)
  }

  var imdbCache = if( Configuration.importIMDBCache.exists() ) {
    System.err.println("Reading cached IMDB database from file...")
    new ObjectInputStream(new FileInputStream(Configuration.importIMDBCache)).readObject().asInstanceOf[HashMap[String, MediaSource]]
  } else {
    HashMap[String, MediaSource]()
  }
  System.err.println("IMDB cache contains %d elements...".format(imdbCache.size))

  /**
   * Get the MediaSource with additional information on the movie/TV show
   * which corresponds to the subtitle file.
   *
   * @param id id of the mediasource from the index file
   * @return
   */
  def loadMediaSource(id: String): MediaSource = subtitles.get(id) match {
    case Some(mediaSource) => MediaSource.fromCachedIMDB(mediaSource.title, mediaSource.year, imdbCache)
    case None => throw new IOException("No movie found in the DB!")
  }


  /**
   * Load the aligned chunk pairs in the folder into the translation memory.
   *
   * @param tm the translation memory instance to be initialized
   * @param folder source folder with aligned subtitle files
   */
  def loadChunks(tm: TranslationMemory, folder: File) {

    tm.reset()
    val heldoutWriter = new PrintWriter(Configuration.heldoutFile)

    var finishedFiles = 0

    System.err.println("Processing files:")
    val inputFiles = folder.listFiles filter(_.getName.endsWith(".txt"))
    inputFiles grouped( Configuration.importBatchSize ) foreach(
      (files: Array[File])=> { tm.add(
        files flatMap ( (sourceFile: File) => {

          val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
          mediaSource.id = tm.mediaStorage.addMediaSource(mediaSource)

          System.err.println( "- %s: %s, %s, %s"
            .format(sourceFile.getName, mediaSource.title, mediaSource.year,
            if (mediaSource.genres.size > 0)
              mediaSource.genres.toString()
            else
              "Could not retrieve additional information")
          )

          //Read all pairs from the file and convert them to translation pairs
          try {
            val pairs = Source.fromFile(sourceFile).getLines()
              .map( TranslationPair.maybeFromString(_) )
              .flatten
              .map( { pair => pair.setMediaSource(mediaSource); pair } )

            //Exclude heldoutSize% of the data as heldout data
            val (training, heldout) =
              pairs.toList.partition(_ => !(Random.nextFloat() < Configuration.heldoutSize) )

            heldout.foreach( pair => heldoutWriter.println(pair.toExternalString) )

            training
          } catch {
            case e: MalformedInputException => {
              System.err.println("Error: Could not read file %s".format(sourceFile))
              List()
            }
          }
        }))
        finishedFiles += files.size
        System.err.println("Processed %d of %d files...".format(finishedFiles, inputFiles.size))

        if ( finishedFiles % (Configuration.importBatchSize * 5) == 0 ) {
          System.err.println("Doing some cleanup...")
          writeIMDBCache()

          val r = Runtime.getRuntime
          System.err.println("Total memory is: %.2fMB".format(r.totalMemory() / (1024.0*1024.0)))
          System.err.println("Free memory is: %.2fMB".format(r.freeMemory() / (1024.0*1024.0)))
          System.err.println("Running GC")
          System.gc(); System.gc(); System.gc(); System.gc()
          System.err.println("Free memory is: %.2fMB".format(r.freeMemory() / (1024.0*1024.0)))
        }
      }
      )

    heldoutWriter.close()

    writeIMDBCache()
  }


  def main(args: Array[String]) {
    loadSubtitleMapping(Configuration.fileMediasourceMapping)
    System.err.println("Loaded subtitle -> movie mapping")

    val tm = Factory.createTM(readOnly = false)
    loadChunks(tm, Configuration.dataFolder)

  }

}
