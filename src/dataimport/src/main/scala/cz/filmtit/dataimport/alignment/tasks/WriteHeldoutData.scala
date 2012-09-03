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

package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter.loadFilePairsToMap

/**
 * Helper object for writing heldout data. We do it by doing an alignment and then writing
 * the unaligned.
 */
object WriteHeldoutData {
  def writeHeldoutData(
                        alignment:SubtitleFileAlignment,
                        choser:GoodFilePairChooser,
                        mapping:SubtitleMapping,
                        conf:Configuration,
                        where:String) {

    val a = new Aligner(alignment, null, choser, conf, Language.EN, Language.CS);
    val alignedMovies = a.alignFiles(mapping).map{_._1.filmID}
    val nonalignedMovies = mapping.moviesWithSubs.toSet -- alignedMovies

    val nonalignedSubtitles = nonalignedMovies.map{
      m=>(m, mapping.getSubtitles(m).find{sf=>sf.language==Some(Language.EN)})}.filter{_._2.isDefined}
    val writer = new java.io.PrintWriter(new java.io.File(where))

    nonalignedSubtitles.foreach{
      case (movie, file)=>writer.println(movie+"\n"+file.get.fileNumber)
    }
    writer.close


  }

  def writeHeldoutData(where:String="heldout") {

    val c = new Configuration("configuration.xml")

    val mapping = new SubtitleMapping(c, false)
    val filename ="aligned"

    val file = new java.io.File(filename)
    val map = loadFilePairsToMap(file, c)

    writeHeldoutData(new SubtitleFileAlignmentFromFile(Language.EN, Language.CS, map), new GoodFilePairChooserFromFile(map), mapping,c, where)


  }

  def main(args:Array[String]) = writeHeldoutData("heldout")

}
