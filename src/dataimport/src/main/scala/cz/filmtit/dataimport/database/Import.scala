/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.dataimport.database


import cz.filmtit.dataimport.SubtitleMapping
import collection.mutable.HashMap
import io.Source
import cz.filmtit.core.model.{MediaSourceFactory, TranslationMemory}
import java.io._
import java.nio.charset.MalformedInputException

import cz.filmtit.share.{MediaSource, TranslationPair}
import java.lang.System
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory
import cz.filmtit.core.{Configuration, Factory}

/**
 * @author Joachim Daiber
 */

class Import(val configuration: Configuration) {


  var subtitles = new SubtitleMapping(configuration, checkForExistenceAndLanguages=false)
  val mediasourceFactory: MediaSourceFactory = new FreebaseMediaSourceFactory(configuration.freebaseKey)

  var hit = 0
  var miss = 0

  var mediasourceCache = if( configuration.importMediasourceCache.exists() ) {
    System.err.println("Reading cached Media Source DB from file...")
    new ObjectInputStream(new FileInputStream(configuration.importMediasourceCache)).readObject().asInstanceOf[HashMap[String, MediaSource]]
  } else {
    HashMap[String, MediaSource]()
  }
  val imdbInitialSize = mediasourceCache.size
  System.err.println("Media source cache contains %d elements...".format(imdbInitialSize))


  def writeIMDBCache() {
    if (imdbInitialSize != mediasourceCache.size) {
      System.err.println("Writing Media Source cache to file...")
      new ObjectOutputStream(new FileOutputStream(configuration.importMediasourceCache)).writeObject(mediasourceCache)
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
    case Some(mediaSource) => mediasourceFactory.getCachedSuggestion(mediaSource.getTitle, mediaSource.getYear, mediasourceCache)
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
    tm.finishImport()

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
