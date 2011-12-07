package cz.fimtit.eval.database

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
    connection.createStatement().execute("CREATE INDEX idx_trigrams ON sentences USING gist (sentence gist_trgm_ops);")

  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence, similarity(sentence, ?) AS sml FROM %s WHERE sentence % ? ORDER BY sml DESC, sentence;".format(tableName))
    select.setString(1, chunk)
    select.setString(2, chunk)
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getFloat("sml") + ": " + rs.getString("sentence"))
    }

    null;
  }


  override def name: String = "Translation pair storage using a trigram index."


}

/**
  * Postgres-based storage using a full-text index.
  */










