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
import scala.collection.Seq
import cz.filmtit.share.parsing.UnprocessedChunk

/**
 * An abstract object for aligning chunk to chunk
 *
 * @constructor create a new chunkAlignment
 * @param l1 first language
 * @param l2 second language
 */
abstract class ChunkAlignment(val l1:Language, val l2:Language)  {

  /**
   * Abstract method that aligns cunk to chunk
   * @param chunksL1 chunks of one file
   * @param chunksL2 chunks of second file
   * @return pairs of aligned chuns
   */
    def alignChunks(chunksL1: Seq[UnprocessedChunk], chunksL2:Seq[UnprocessedChunk]):List[Pair[UnprocessedChunk, UnprocessedChunk]];
    

}
