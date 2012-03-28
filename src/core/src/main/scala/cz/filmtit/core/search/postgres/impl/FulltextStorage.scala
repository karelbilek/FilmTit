package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseStorage
import cz.filmtit.core.model.data.{Chunk, ScoredTranslationPair,
TranslationPair}
import cz.filmtit.core.model.{TranslationSource, Language}


/**
 * Postgres-based storage using a full-text index.
 */
class FulltextStorage(l1: Language, l2: Language, readOnly: Boolean = true)
  extends BaseStorage(l1, l2, TranslationSource.InternalFuzzy, readOnly = readOnly) {


  override def candidates(chunk: Chunk, language: Language): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM %s WHERE to_tsvector('english', sentence) @@ plainto_tsquery('english', ?);".format(pairTable))
    select.setString(1, chunk)
    val rs = select.executeQuery()

    while (rs.next) {
      println(" " + rs.getString("sentence"))
    }

    null
  }


  override def name: String = "Translation pair storage using a full text index."

  def reindex() {
    connection.createStatement().execute(("DROP index IF EXISTS idx_fulltext;" +
      "CREATE INDEX idx_fulltext ON %s USING gin(to_tsvector('english', sentence));").format(pairTable))
  }
}
