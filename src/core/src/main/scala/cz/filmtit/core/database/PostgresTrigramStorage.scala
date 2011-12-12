package cz.filmtit.core.database

import cz.filmtit.core.model._


/**
 * @author Joachim Daiber
 *
 */


class PostgresTrigramStorage extends PostgresStorage with TranslationPairStorage {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {

    createTable(translationPairs);

    //Create the index:
    println("Creating index...")
    connection.createStatement().execute("DROP INDEX IF EXISTS idx_trigrams; CREATE INDEX idx_trigrams ON %s USING gist (sentence gist_trgm_ops);".format(tableName))

  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM "+tableName+" WHERE sentence % ?;")
    select.setString(1, chunk)
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getString("sentence"))
    }

    null;
  }


  override def name: String = "Translation pair storage using a trigram index."


}
