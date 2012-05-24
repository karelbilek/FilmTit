package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseStorage
import cz.filmtit.share.{Language, TranslationPair, Chunk, TranslationSource}
import cz.filmtit.core.Configuration

import java.sql.Connection

/**
 * Postgres-based retrieval via vector-based full text search.
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

}
