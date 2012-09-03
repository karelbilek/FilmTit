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

import _root_.java.sql.Connection
import cz.filmtit.core.search.postgres.BaseStorage
import cz.filmtit.share.{TranslationSource, TranslationPair, Language, Chunk}

/**
 * An entirely Postgres-based first letter translation pair searcher.
 *
 * @author Joachim Daiber
 */

class PGFirstLetterStorage(
  l1: Language,
  l2: Language,
  connection: Connection,
  useInMemoryDB: Boolean = false
) extends BaseStorage(
  l1,
  l2,
  TranslationSource.INTERNAL_EXACT,
  connection,
  useInMemoryDB
) {

  override def warmup() {}

  override def reindex() {
    log.info("Reindexing...")

    connection.createStatement().execute("""
      DROP FUNCTION IF EXISTS firstletter(TEXT);
      CREATE FUNCTION firstletter(TEXT) RETURNS TEXT
          AS $$

      	DECLARE
      		retVal TEXT[];
      		tokens TEXT[];
      	BEGIN
      		tokens := array(SELECT token FROM ts_parse('default', $1) WHERE tokid != 12);
          IF array_length(tokens, 1) IS NULL or array_length(tokens, 1) = 0 THEN
            RETURN '';
          END IF;

      		FOR I IN 1..array_length(tokens, 1) LOOP
      			IF array_length(tokens, 1) = 1 THEN
      				retVal[I] := lower(tokens[I]);
      			ELSIF array_length(tokens, 1) = 2 THEN
      				retVal[I] := lower(substring(tokens[I] from 0 for 4));
      			ELSIF array_length(tokens, 1) = 3 THEN
      				retVal[I] := lower(substring(tokens[I] from 0 for 3));
      			ELSE
      				retVal[I] := lower(substring(tokens[I] from 0 for 2));
      			END IF;
      		END LOOP;

      		RETURN array_to_string(retVal, ' ');
      	END;
          $$ LANGUAGE 'plpgsql' IMMUTABLE;
     """.stripMargin)

    connection.createStatement().execute(("DROP index IF EXISTS idx_firstletter_l1;" +
      "CREATE INDEX idx_firstletter_l1 ON %s (firstletter(chunk_l1), pair_count DESC);").format(pairTable))
    connection.createStatement().execute(("DROP index IF EXISTS idx_firstletter_l2;" +
      "CREATE INDEX idx_firstletter_l2 ON %s (firstletter(chunk_l2), pair_count DESC);").format(pairTable))
  }

  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val searchColumn = if(language.equals(l1)) "chunk_l1" else "chunk_l2"
    val select = connection.prepareStatement("SELECT * FROM "+pairTable+" WHERE firstletter("+searchColumn+") = firstletter(?) LIMIT "+this.maxCandidates+";")
    select.setString(1, chunk.getSurfaceForm)

    val rs = select.executeQuery()
    var tps = List[TranslationPair]()

    while (rs.next) {
      val c1 = if(language.equals(l1)) new Chunk(rs.getString("chunk_l1")) else new Chunk(rs.getString("chunk_l2"))
      val c2 = if(language.equals(l1)) new Chunk(rs.getString("chunk_l2")) else new Chunk(rs.getString("chunk_l1"))
      val tp = new TranslationPair(c1, c2, TranslationSource.INTERNAL_EXACT)
      tp.setId(rs.getInt("pair_id"))
      tp.setCount(rs.getInt("pair_count"))
      tps ::= tp
    }

    tps
  }

  override def name: String = "Translation pair storage using exact postgres based."

  override def requiresTokenization = false

}