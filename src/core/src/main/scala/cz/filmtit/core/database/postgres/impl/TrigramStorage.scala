package cz.filmtit.core.database.postgres.impl

import cz.filmtit.core.model._
import cz.filmtit.core.model.Language.Language
import cz.filmtit.core.database.postgres.BaseStorage
import data.{Chunk, ScoredTranslationPair, TranslationPair}


/**
 * @author Joachim Daiber
 *
 */


class TrigramStorage(l1: Language, l2: Language) extends BaseStorage(l1, l2) {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
    createChunks(translationPairs);
    reindex()
  }


  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk, language: Language): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM " +
      "" + chunkTable + " WHERE sentence % ?;")
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
        "gist (sentence gist_trgm_ops);").format(chunkTable))
  }
}
