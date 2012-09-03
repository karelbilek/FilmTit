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

package cz.filmtit.dataimport

import alignment.io.SubtitleFile
import io.Source
import cz.filmtit.share.MediaSource
import collection.mutable.ListBuffer
import collection.mutable.HashMap
import java.io.File
import cz.filmtit.share.Language
import cz.filmtit.core.Configuration

/**
 * Object that holds all the information from export.txt file
 * basically mapping both from movie ID to movie as mediasource
 * but also mapping from movie ID to list of subtitle files.
 *
 * I am using "Movie" and "Mediasource" interchangably in this doc.
 *
 * @param conf configuration, determining where is the file
 * @param checkForExistenceAndLanguages whether the SubtitleMapping should go through the files,
 *                                      check if they exists and are correctly gunzippable, check for .srt
 *                                      correctness and lastly determine the language.
 */
class SubtitleMapping(val conf:Configuration, val checkForExistenceAndLanguages:Boolean) {

  /**
   * Actual map of subtitles.
   * For every media source ID we have pair of actual mediasource object and list of subtitles
   * in both languages.
   */
  val subtitles = HashMap[String, Pair[MediaSource, ListBuffer[SubtitleFile]]]()

   //subtitle mapping is loaded directly at its construction
   {
     //the exact file has to be set in config.xml
     val mappingFile = conf.fileMediasourceMapping
     println("mapping file: "+mappingFile)

     //do for every line...
     Source.fromFile(mappingFile).getLines() foreach { line =>
        val data = line.split("\t")
        val mediaSourceID = data(0)

        val mediaSource = new MediaSource(
              data(7),   //mediaSourceID
              data(8),   //year
              "");       //genres


        //checking for languages if we want to do so
        //It is option, because it is None if the file with the given mediaSourceID
        //doesn't exist.
        val subtitleFile: Option[SubtitleFile] = if (checkForExistenceAndLanguages) {
            SubtitleFile.fileIfExists(conf, mediaSourceID, data(1), true)
        } else {
            //I don't check for existence or language => I got the language from export file
            val language = if (data(2) == "eng") {Language.EN} else {Language.CS}
            SubtitleFile.fileIfExists(conf, mediaSourceID, data(1), false, putLanguage = Some(language))
        }

        //creating the hashMap.... could probably be more functional :)
        if (!subtitles.contains(mediaSourceID)) {
           if (subtitleFile.isDefined) {
             subtitles.put(mediaSourceID,(mediaSource, ListBuffer(subtitleFile.get)))
             
           } else {
             subtitles.put(mediaSourceID,(mediaSource, ListBuffer[SubtitleFile]()))
           }
        } else {
           if (subtitleFile.isDefined) {
             subtitles.get(mediaSourceID).get._2 += subtitleFile.get
           }
 
        }
     }
   }

  /**
   * Gives a media source object for a given mediasource ID
   * @param mediaSourceID Which movie to return
   * @return actual MediaSource object
   */
  def getMediaSource(mediaSourceID:String):Option[MediaSource] = {
    if (subtitles.get(mediaSourceID).isDefined) {
        Some(subtitles.get(mediaSourceID).get._1)
    } else {
        None
    }
  }

  /**
   * Get all subtitles for a given mediasource ID
   * @param mediaSourceID For which movie to take subtitles
   * @return List of subtitles. Empty, if the mediasource is not in there at all.
   */
  def getSubtitles(mediaSourceID:String):List[SubtitleFile] = {
    if (subtitles.get(mediaSourceID).isDefined) {
       subtitles.get(mediaSourceID).get._2.toList
    } else {
        List[SubtitleFile]()
    }
  }

  /**
   * Checks whether file with a given mediasource ID has all the languages from list.
   * @param mediaSourceID mediasource ID
   * @param languagesToCheck Which all languages have to be in language set?
   * @return True iff there are subtitles of all the languages for a given movie.
   */
  def hasSubtitlesLanguages(mediaSourceID:String, languagesToCheck:Iterable[Language]):Boolean = {
     val optSubs:List[SubtitleFile] = getSubtitles(mediaSourceID)

     //slightly unreadable way of getting all the languages for one file
     val languages:Set[Language] = optSubs.map{_.language}.filter{_.isDefined}.map{_.get}.toSet

     //check for all languages for one file
     languagesToCheck.forall {
       l=>languages.contains(l)
     }
  }

  /**
   * Does a given movie have any subtitles?
   * @param mediaSourceID mediasource ID of the movie
   * @return True iff the movie has at least 1 subtitle of any language
   */
  def hasSubtitles(mediaSourceID:String):Boolean = {
     val subs = getSubtitles(mediaSourceID)

     if (subs.isEmpty) {
          false
     } else {
          true
     }

  }

  /**
   * ID of all the movies. Includes those with 0 subtitles.
   * @return IDs of all the movies.
   */
  def movies:Iterable[String] = subtitles.keys

  /**
   * All movies that have at least 1 subtitle of any language.
   * @return IDs of all movies that have at least 1 subtitle of any language.
   */
  def moviesWithSubs():Iterable[String] = movies.filter{hasSubtitles(_)}

  /**
   * All movies that have at least one subtitle of both English and Czech
   * (or any other language in conf -- but other langs are not tested)
   * @return IDs of all movies that have at least one subtitle of both English and Czech
   */
  def moviesWithSubsBothLangs():Iterable[String] =
    movies.filter{hasSubtitlesLanguages(_, Iterable[Language](conf.l1, conf.l2))}

  /**
   * All movies that have at least one subtitle of English
   * (or any other language in conf)
   * @return IDs of all movies that have at least one subtitle of English
   */
  def moviesWithSubsEn():Iterable[String] =
    movies.filter{hasSubtitlesLanguages(_,
      Iterable[Language](conf.l1))}
}
