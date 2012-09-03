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

package cz.filmtit.core

import cz.filmtit.share.Chunk

/**
 * General Utils used in the Scala code.
 *
 * @author Joachim Daiber
 */

object Utils {

  /**
   * map method for Tuples
   *
   * e.g. (1, 2) map (_ * 2)
   **/
  implicit def t2mapper[X, A <: X, B <: X](t: (A,B)) = new {
    def map[R](f: X => R) = (f(t._1), f(t._2))
  }

  implicit def chunkFromString(surfaceform: String): Chunk = new Chunk(surfaceform)

  def min(nums: Int*): Int = nums.min

}
