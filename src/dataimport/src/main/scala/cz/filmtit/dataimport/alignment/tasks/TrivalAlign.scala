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
import cz.filmtit.dataimport.alignment.model.Aligner
import cz.filmtit.dataimport.alignment.aligners.trivial._
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._

/**
 * Object with main class for making a trivial alignment
 * (for baseline, say)
 */
object TrivialAlign {

  /**
   * Run the trivial aligning
   * @param args
   */
  def main(args: Array[String]) = {
    val config = new Configuration(args(0))
    val where = args(1)

    val aligner = new Aligner(
      new TrivialSubtitleFileAlignment(Language.EN, Language.CS),
      new TrivialChunkAlignment(Language.EN, Language.CS),
      new TrivialGoodFilePairChooser,
      config,
      Language.EN, Language.CS
    )

    writeFilePairsToFile(aligner.alignFiles(new SubtitleMapping(config, true)), new java.io.File(where));
  }
}
