package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.model._
import cz.filmtit.core.search.postgres.BaseStorage
import data.{Chunk, ScoredTranslationPair, TranslationPair}


/**
 * Postgres-based retrieval via vector-based full text search.
 *
 * @author Joachim Daiber
 */


class TrigramStorage(l1: Language, l2: Language, readOnly: Boolean = true)
  extends BaseStorage(l1, l2, TranslationSource.InternalFuzzy, readOnly = readOnly) {

  override def candidates(chunk: Chunk, language: Language): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM " +
      "" + pairTable + " WHERE sentence % ?;")
    select.setString(1, chunk)
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
