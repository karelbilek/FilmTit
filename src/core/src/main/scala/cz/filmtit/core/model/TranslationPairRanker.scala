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

package cz.filmtit.core.model

import cz.filmtit.share.{TranslationPair, MediaSource, Chunk}

/**
 * Assigns scores to translation pairs retrieved from the database or external
 * services and orders them by their score.
 *
 * @author Joachim Daiber
 */

abstract class TranslationPairRanker {


  /**
   * Rank the list of translation pairs according to a score of how well
   * they match the Chunk and source MediaSource. The best match will
   * be the first in the list.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pairs the translation pair candidates
   * @return sorted list of scored translation pairs with best first
   */
  def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
  List[TranslationPair] = pairs.map(pair => rankOne(chunk, mediaSource, pair)).sorted


  /**
   * Return only the best translation pair for the Chunk and MediaSource.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pairs the translation pair candidates
   * @return best-match translation pair with score
   */
  def best(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
  Option[TranslationPair] =
    pairs.map(pair => rankOne(chunk, mediaSource, pair)) match {
      case List()                                    => None
      case x: List[TranslationPair] => Some(x.max)
    }



  /**
   * Scores a single translation pair according to match with Chunk and
   * MediaSource.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pair the translation pair candidate
   * @return translation pair with score
   */
  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair


  /**
   * Every ranker should have a name.
   */
  def name: String

}

