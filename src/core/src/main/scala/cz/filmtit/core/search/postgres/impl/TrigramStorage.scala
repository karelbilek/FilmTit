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

package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseStorage
import cz.filmtit.share.{Language, TranslationPair, Chunk, TranslationSource}
import cz.filmtit.core.Configuration

import java.sql.Connection

/**
 * Postgres-based retrieval via fulltext search based on character trigrams.
 *
 * @author Joachim Daiber
 */

class TrigramStorage(
  l1: Language,
  l2: Language,
  connection: Connection,  
  hssql: Boolean = false
) extends BaseStorage(
  l1,
  l2,
  TranslationSource.INTERNAL_FUZZY,
  connection,
  hssql
) {

  override def warmup() {}

  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM " +
      "" + pairTable + " WHERE sentence % ?;")
    select.setString(1, chunk.getSurfaceForm)
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getString("sentence"))
    }

    null;
  }


  override def name: String = "Translation pair storage using a trigram index."

  def reindex() {
    connection.createStatement().execute(
      ("DROP INDEX IF EXISTS idx_trigrams; CREATE INDEX idx_trigrams ON %s USING " +
        "gist (sentence gist_trgm_ops);").format(pairTable))
  }

  override def requiresTokenization = false

}
