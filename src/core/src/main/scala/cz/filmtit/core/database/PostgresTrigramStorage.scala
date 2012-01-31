package cz.filmtit.core.database

import cz.filmtit.core.model._
import cz.filmtit.core.model.Language._


/**
 * @author Joachim Daiber
 *
 */


class PostgresTrigramStorage(l1: Language, l2: Language) extends PostgresStorage(l1, l2) {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {

    createChunkTables(translationPairs);

    //Create the index:
    println("Creating index...")
    connection.createStatement().execute("DROP INDEX IF EXISTS idx_trigrams; CREATE INDEX idx_trigrams ON %s USING gist (sentence gist_trgm_ops);".format(tableNameChunks))

  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk, language: Language): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM "+tableNameChunks+" WHERE sentence % ?;")
    select.setString(1, chunk)
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getString("sentence"))
    }

    null;
  }


  override def name: String = "Translation pair storage using a trigram index."


}
