package cz.filmtit.core.database

import cz.filmtit.core.model._
import cz.filmtit.core.model.Language._


/**
  * Postgres-based storage using a full-text index.
  */
class PostgresFulltextStorage(l1: Language, l2: Language) extends PostgresStorage(l1, l2) {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
    createChunks(translationPairs);
    reindex()
  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk, language: Language): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM %s WHERE to_tsvector('english', sentence) @@ plainto_tsquery('english', ?);".format(chunkTable))
    select.setString(1, chunk)
    val rs = select.executeQuery()

    while (rs.next) {
      println(" " + rs.getString("sentence"))
    }

    null;
  }


  override def name: String = "Translation pair storage using a full text index."

  def reindex() {
    connection.createStatement().execute(("DROP index IF EXISTS idx_fulltext;" +
      "CREATE INDEX idx_fulltext ON %s USING gin(to_tsvector('english', sentence));").format(chunkTable))
  }
}
