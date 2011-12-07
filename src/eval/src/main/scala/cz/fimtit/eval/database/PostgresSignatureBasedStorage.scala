package cz.fimtit.eval.database

import cz.filmtit.core.model._


/**
 * @author Joachim Daiber
 *
 */

abstract class PostgresSignatureBasedStorage
  extends PostgresStorage
  with TranslationPairStorage
  with SignatureBasedStorage {

  def indexName: String

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
    connection.createStatement().execute("DROP TABLE IF EXISTS %s; CREATE TABLE sentences (signature VARCHAR(100), sentence VARCHAR(10000));".format(tableName))

    val insertStatement = connection.prepareStatement("INSERT INTO %s (signature, sentence) VALUES (?, ?)".format(tableName))

    println("Reading sentences...")
    translationPairs foreach {translationPair => {
      insertStatement.setString(1, signature(translationPair.source))
      insertStatement.setString(2, translationPair.source)
      insertStatement.execute()
    }
    }

    //Create the index:
    println("Creating index...")
    connection.createStatement().execute("CREATE INDEX %s ON %s (signature);".format(indexName, tableName))

  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM %s WHERE signature = ?;".format(tableName))
    select.setString(1, signature(chunk))
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getString("sentence"))
    }

    null;
  }

  def signature(sentence: Chunk): String

}
