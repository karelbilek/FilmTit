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

package cz.filmtit.core.merge

import cz.filmtit.core.Utils._
import cz.filmtit.share.TranslationPair
import cz.filmtit.core.model.TranslationPairMerger

/**
 * A [[cz.filmtit.core.model.TranslationPairMerger]] based on Levenshtein distance.
 *
 * @author Joachim Daiber
 */

class LevenshteinMerger(minEditDistance: Int = 1) extends TranslationPairMerger {

  def levenshteinSmallerN(str1: String, str2: String, minDistance: Int): Boolean = {

    val lenStr1 = str1.length
    val lenStr2 = str2.length

    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)

    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j

    for (i <- 1 to lenStr1) {
      for (j <- 1 to lenStr2) {
        val cost = if (str1(i - 1) == str2(j-1)) 0 else 1

        d(i)(j) = min(
          d(i-1)(j  ) + 1,     // deletion
          d(i  )(j-1) + 1,     // insertion
          d(i-1)(j-1) + cost   // substitution
        )
      }

      if (d(i).min > minEditDistance) {
        return false
      }

    }

    d(lenStr1)(lenStr2) <= minEditDistance
  }

  def merge(pairs: List[TranslationPair], n: Int): List[TranslationPair] = {

    var pairsToBeRemoved = Set[Int]()

    for (i <- (0 to pairs.size-1)) {
      if (!pairsToBeRemoved.contains(i)) {
        for (j <- (i+1 to pairs.size-1)) {
          if (levenshteinSmallerN(pairs(i).getStringL2, pairs(j).getStringL2, minEditDistance)) {
            pairsToBeRemoved += j
          }
        }
      }
    }

    var mergedPairs = List[TranslationPair]()
    var i = 0
    for (pair <- pairs) {
      if (!pairsToBeRemoved.contains(i))
        mergedPairs :+= pair
      i += 1
    }

    mergedPairs.take(n)
  }

}