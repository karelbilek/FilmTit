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

package cz.filmtit.core.model.storage

import cz.filmtit.share.Chunk
import cz.filmtit.share.Language

/**A special case of [[cz.filmtit.core.model.TranslationPairStorage]], in which the
 * candidates are retrieved and indexed using a signature string.
 *
 * @author Joachim Daiber
 */
trait SignatureTranslationPairStorage extends TranslationPairStorage {

  /**A signature String for a specific [[cz.filmtit.share.Chunk]] used to index and retrieve it. */
  def signature(sentence: Chunk, language: Language): Signature

}
