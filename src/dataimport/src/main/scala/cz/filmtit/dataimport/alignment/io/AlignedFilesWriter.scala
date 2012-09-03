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

package cz.filmtit.dataimport.alignment.io

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import scala.Some
import java.io.File

/**
 * Helper object for saving aligned subtitle files to a file.
 * It is useful for "tuning" the subtitle-to-subtitle alignment, so we don't have
 * to do the file-to-file alignment again.
 */
object AlignedFilesWriter {
  /**
   * Writes pairs to a given printwriter.
   * @param pairs pairs of object SubtitleFile
   * @param writer Java's PrintWriter to write to
   */
  def writeFilePairsToPrintWriter(
                                   pairs: Iterable[Pair[SubtitleFile, SubtitleFile]],
                                   writer: java.io.PrintWriter) {
    pairs.foreach {
      case Pair(sf1, sf2) =>
        writer.println(sf1.filmID + "\t" + sf1.fileNumber + "\t" + sf2.fileNumber)
        println(sf1.filmID + "\t" + sf1.fileNumber + "\t" + sf2.fileNumber)

    }
  }

  /**
   * Writes pairs to a given file.
   * @param pairs pairs of object SubtitleFile
   * @param where File to write to
   */
  def writeFilePairsToFile(pairs: Iterable[Pair[SubtitleFile, SubtitleFile]], where: java.io.File) = {
    val writer = new java.io.PrintWriter(where)
    writeFilePairsToPrintWriter(pairs, writer)
    writer.close
  }

  /**
   * Reads file pairs from a file
   * @param where File from which to read the pairs
   * @param conf Configuration
   * @param l1 First language
   * @param l2 Second language
   * @param includeNonExistingFiles should I include non-existing files too or not?
   * @return File pairs, as saved to the file.
   */
  def readFilePairsFromFile(where: java.io.File,
                            conf: Configuration,
                            l1: Language, l2: Language,
                            includeNonExistingFiles: Boolean): Iterable[Pair[SubtitleFile, SubtitleFile]] = {
    val reg = """(.*)\t(.*)\t(.*)""".r
    io.Source.fromFile(where).getLines().toIterable.flatMap {
      case reg(movie, descrL, descrR) =>
        val sub1 = SubtitleFile.fileIfExists(conf, movie, descrL, !includeNonExistingFiles, Some(l1))
        val sub2 = SubtitleFile.fileIfExists(conf, movie, descrR, !includeNonExistingFiles, Some(l2))
        if (sub1.isDefined && sub2.isDefined) {
          Some((sub1.get, sub2.get))
        } else {
          None
        }
    }
  }

  /**
   * Reads file pairs from a file
   * @param f Where to read the file pairs from
   * @param c Configuration. By a mistake, the l1 and l2 are switched now, but I don't want to break something
   *          else now by correcting it
   * @return map of movie ID => best subtitle pair
   */
  def loadFilePairsToMap(f:File, c:Configuration):Map[String, Pair[SubtitleFile, SubtitleFile]] = {

    //for reasons I don't remember anymore the languages are switched here

    println("zacinam cist ze souboru")
    val iterable = readFilePairsFromFile(f, c, c.l2, c.l1, true)
    println("pulka")
    iterable.map{case(f1,f2)=>(f1.filmID, (f1,f2))}.toMap
  }
}
