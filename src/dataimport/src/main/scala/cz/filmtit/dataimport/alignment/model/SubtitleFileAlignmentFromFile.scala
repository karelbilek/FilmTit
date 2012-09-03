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
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * Helper class that aligns the files the same way as is saved in the file.
 * @param l1 first language
 * @param l2 second language
 * @param map Map loaded from the file
 */
class SubtitleFileAlignmentFromFile(l1:Language, l2:Language, val map: Map[String, Pair[SubtitleFile, SubtitleFile]]) extends SubtitleFileAlignment(l1,l2) {

       def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {
        if (map.contains(filesL1(0).filmID)) {
            return map(filesL1(0).filmID)
        } else {
            (filesL1(0), filesL2(0))
        }
    }

}
