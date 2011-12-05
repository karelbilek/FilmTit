package cz.fimtit.eval.database

import java.sql.{Statement, DriverManager}
import cz.filmtit.core.model.{ScoredTranslationPair, TranslationPair, TranslationPairStorage}


/**
 * @author Joachim Daiber
 *
 */

abstract class PostgresStorage {

  //Load the driver:
  classOf[org.postgresql.Driver]

  //Initialize connection
  val connection = DriverManager.getConnection("jdbc:postgresql://localhost/filmtit", "postgres", "postgres")

}


class PostgresTrigramStorage extends PostgresStorage with TranslationPairStorage {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
    connection.createStatement().execute("DROP TABLE IF EXISTS sentences; CREATE TABLE sentences (sentence VARCHAR(10000));")

    val insertStatement = connection.prepareStatement("INSERT INTO sentences (sentence) VALUES (?)")

    println("Reading sentences...")
    translationPairs.foreach(
      translationPair => {
        insertStatement.setString(1, translationPair.asInstanceOf[TranslationPair].sourceSentence)
        insertStatement.execute()
      }
    )
    //insertStatement.executeBatch()

    //Create the index:
    println("Creating index...")
    connection.createStatement().execute("CREATE INDEX idx_sentences ON sentences USING gist (sentence gist_trgm_ops);")

  }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(sentence: String): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence, similarity(sentence, ?) AS sml FROM sentences WHERE sentence % ? ORDER BY sml DESC, sentence;")
    select.setString(1, sentence)
    select.setString(2, sentence)
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getFloat("sml") + ": " + rs.getString("sentence"))
    }

    null;
  }


  override def name(): String = "Translation pair storage using a trigram index."


}



object JDBCEvaluator {
  def main(args: Array[String]) {
    val postgresTrigramStorage: PostgresTrigramStorage = new PostgresTrigramStorage()

    //postgresTrigramStorage.initialize(
    //  scala.io.Source.fromFile("/Users/jodaiber/Desktop/src/FilmTit/dbms_experiments/corpus.1m.txt").getLines().map(
    //    line => { new TranslationPair(line.trim(), null) }
    //  )
    //)

    new PostgresTrigramStorage().candidates("Germany won the world cup.")
  }
}