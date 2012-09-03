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

package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/** An abstract object for aligning file to file
 *
 * @constructor create a new SubtitleFileAlignment
 * @param l1 first language
 * @param l2 second language
 */
abstract class SubtitleFileAlignment(val l1:Language, val l2:Language) {

  /**
   * Aligns file to file
   * @param files list of files of both languages
   * @return Either None if there is no file of either language or third language, or
   *         returns the pair of subtitle files
   */
    def alignFiles(files:List[SubtitleFile]):Option[Pair[SubtitleFile, SubtitleFile]] = {
        val filesL1 = new ListBuffer[SubtitleFile]()
        val filesL2 = new ListBuffer[SubtitleFile]()
        files.foreach{f=>
            
            println(" vec")

            if (f.language == Some(l1)) {
                filesL1.append(f);
            } else if (f.language == Some(l2)) {
                
                filesL2.append(f);
            } else {
                return None
            }
        }
        
        if (filesL1.isEmpty) return None
        if (filesL2.isEmpty) return None
        
        Some(alignFiles(filesL1.toList, filesL2.toList))

    }

  /**
   * Aligns file to file, abstract method
   * @param filesL1 list of files of first language
   * @param filesL2 list of files of second language
   * @return Pair of best subtitle files
   */
    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile];

}
