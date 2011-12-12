package cz.filmtit.core.database

import cz.filmtit.core.model._

/**
  * Postgres-based storage using a full-text index.
  */
class PostgresFulltextStorage extends PostgresStorage with TranslationPairStorage {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {

    createTable(translationPairs);

    //Create the index:
    println("Creating index...")
    connection.createStatement().execute("drop index if exists idx_fulltext; CREATE INDEX idx_fulltext ON %s USING gin(to_tsvector('english', sentence));".format(tableName))

  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM %s WHERE to_tsvector('english', sentence) @@ plainto_tsquery('english', ?);".format(tableName))
    select.setString(1, chunk)
    val rs = select.executeQuery()

    while (rs.next) {
      println(" " + rs.getString("sentence"))
    }

    null;
  }


  override def name: String = "Translation pair storage using a full text index."


}
