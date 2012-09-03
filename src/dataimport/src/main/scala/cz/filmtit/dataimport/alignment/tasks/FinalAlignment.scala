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

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.aligners.distance.LinearSubtitlePairCounter
import cz.filmtit.dataimport.alignment.aligners.levensthein._
import java.io.File
import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._

/**
 * Task object, that just does the one, final alignment, without any second guesses
 */
object FinalAlignment {
  def main(args:Array[String]) {
    val c = new Configuration(args(0))
    val l1 = c.l1
    val l2 = c.l2
    val tolerance = 6000L

    val mapping = new SubtitleMapping(c, true)
    println("mapping done")
    
    
    val counter = new LevenstheinDistanceCounter(tolerance)


    val aligner = new Aligner(
      new LevenstheinSubtitleFileAlignment(l1,l2, counter),
      new LevenstheinChunkAlignment(l1, l2, tolerance),
      new LevenstheinGoodFilePairChooser(counter),
      c,
      l1, l2
    )



    println("before aligning")
    aligner.align(mapping)
    println("after aligning")
  }
}
