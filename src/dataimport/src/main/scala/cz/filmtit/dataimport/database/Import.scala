package cz.filmtit.dataimport.database


import cz.filmtit.dataimport.SubtitleMapping
import collection.mutable.HashMap
import io.Source
import cz.filmtit.core.model.TranslationMemory
import scala.util.Random
import java.io._
import java.nio.charset.MalformedInputException

import cz.filmtit.share.{MediaSource, TranslationPair}
import java.lang.System
import cz.filmtit.core.model.data.MediaSourceFactory
import cz.filmtit.core.{Configuration, Factory}

/**
 * @author Joachim Daiber
 */

class Import(val configuration: Configuration) {


  var subtitles = new SubtitleMapping(configuration, checkForExistenceAndLanguages=false)
//  subtitles.load()
  

  var hit = 0
  var miss = 0

  var imdbCache = if( configuration.importIMDBCache.exists() ) {
    System.err.println("Reading cached IMDB database from file...")
    new ObjectInputStream(new FileInputStream(configuration.importIMDBCache)).readObject().asInstanceOf[HashMap[String, MediaSource]]
  } else {
    HashMap[String, MediaSource]()
  }
  val imdbInitialSize = imdbCache.size
  System.err.println("IMDB cache contains %d elements...".format(imdbInitialSize))


  def writeIMDBCache() {
    if (imdbInitialSize != imdbCache.size) {
      System.err.println("Writing cached IMDB database to file...")
      new ObjectOutputStream(new FileOutputStream(configuration.importIMDBCache)).writeObject(imdbCache)
    }
  }

  /**
   * Get the MediaSource with additional information on the movie/TV show
   * which corresponds to the subtitle file.
   *
   * @param id id of the mediasource from the index file
   * @return
   */
  def loadMediaSource(id: String): MediaSource = subtitles.getMediaSource(id) match {
    case Some(mediaSource) => MediaSourceFactory.fromCachedIMDB(mediaSource.getTitle, mediaSource.getYear, imdbCache)
    case None => throw new IOException("No movie found in the DB for id "+id)
  }


  /**
   * Load the aligned chunk pairs in the folder into the translation memory.
   *
   * @param tm the translation memory instance to be initialized
   * @param folder source folder with aligned subtitle files
   */
  def loadChunks(tm: TranslationMemory, folder: File) {

    tm.reset()

    var finishedFiles = 0

    System.err.println("Processing files:")
    val inputFiles = folder.listFiles filter(_.getName.endsWith(".txt"))

    if (!folder.exists() || inputFiles.size == 0)
      throw new IOException("Couldn't find or read data folder.")

    inputFiles grouped( configuration.importBatchSize ) foreach(
      (files: Array[File])=> { tm.add(
        files flatMap ( (sourceFile: File) => {

          val mediaSource = loadMediaSource(sourceFile.getName.replace(".txt", ""))
          mediaSource.setId(tm.mediaStorage.addMediaSource(mediaSource))

          System.err.println( "- %s: %s, %s, %s"
            .format(sourceFile.getName, mediaSource.getTitle, mediaSource.getYear,
            if (mediaSource.getGenres.size > 0)
              mediaSource.getGenres.toString
            else
              "Could not retrieve additional information")
          )

          //Read all pairs from the file and convert them to translation pairs
          try {
            val pairs = Source.fromFile(sourceFile).getLines()
              .map( (s: String) => TranslationPair.fromString(s) )
              .filter(_ != null)
              .map( (pair: TranslationPair) => { pair.addMediaSource(mediaSource); pair })

            pairs
          } catch {
            case e: MalformedInputException => {
              System.err.println("Error: Could not read file %s".format(sourceFile))
              List()
            }
          }
        }))

        finishedFiles += files.size
        System.err.println("Processed %d of %d files...".format(finishedFiles, inputFiles.size))

        if ( finishedFiles % (configuration.importBatchSize * 5) == 0 ) {
          System.err.println("Doing some cleanup...")
          //writeIMDBCache()

          val r = Runtime.getRuntime
          System.err.println("Total memory is: %.2fMB".format(r.totalMemory() / (1024.0*1024.0)))
          System.err.println("Free memory is:  %.2fMB".format(r.freeMemory() / (1024.0*1024.0)))
          System.err.println("Running GC")
          System.gc(); System.gc(); System.gc(); System.gc()
          System.err.println("Free memory is:  %.2fMB".format(r.freeMemory() / (1024.0*1024.0)))
        }
      }
      )


    writeIMDBCache()
    tm.close()
  }
 }

 object Import {
  def main(args: Array[String]) {
    val configuration = new Configuration(new File(args(0)))
    val imp = new Import(configuration)

//    imp.loadSubtitleMapping(configuration.fileMediasourceMapping)
    System.err.println("Loaded subtitle -> movie mapping")

    val tm = Factory.createTMFromConfiguration(configuration, readOnly = false)
    imp.loadChunks(tm, configuration.dataFolder)

  }

}
