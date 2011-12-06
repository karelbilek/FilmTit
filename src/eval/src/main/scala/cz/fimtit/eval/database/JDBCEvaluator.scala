package cz.fimtit.eval.database

import java.sql.{Statement, DriverManager}
import cz.filmtit.core.model._


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
        insertStatement.setString(1, translationPair.sourceSentence)
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

  override def candidates(chunk: Chunk): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence, similarity(sentence, ?) AS sml FROM sentences WHERE sentence % ? ORDER BY sml DESC, sentence;")
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

abstract class PostgresSignatureBasedStorage
  extends PostgresStorage with TranslationPairStorage with SignatureBasedStorage {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
     connection.createStatement().execute("DROP TABLE IF EXISTS sentences; CREATE TABLE sentences (signature VARCHAR(100), sentence VARCHAR(10000));")

     val insertStatement = connection.prepareStatement("INSERT INTO sentences (signature, sentence) VALUES (?, ?)")

     println("Reading sentences...")
     translationPairs.foreach(
       translationPair => {
         insertStatement.setString(1, signature(translationPair.sourceSentence))
         insertStatement.setString(2, translationPair.sourceSentence)
         insertStatement.execute()
       }
     )

     //Create the index:
     println("Creating index...")
     connection.createStatement().execute("CREATE INDEX idx_sentences ON sentences (signature);")

   }

  override def addTranslationPair(translationPair: TranslationPair) {

  }

  override def candidates(chunk: Chunk): List[ScoredTranslationPair] = {
    val select = connection.prepareStatement("SELECT sentence FROM sentences WHERE signature = ?;")
    select.setString(1, signature(chunk))
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getString("sentence"))
    }

    null;
  }

  def signature(sentence: Chunk): String

}

class PostgresFirstLetterStorage extends PostgresSignatureBasedStorage {

  /**
   * Use the lowercased first letter of each word in the sentence as the signature.
   */
  override def signature(chunk: Chunk): String = {
    new String(chunk.surfaceform.split(" ").map( word => word.charAt(0).toLower ))
  }

  override def name: String = "Translation pair storage using the first letter of every word as an index."

}

object JDBCEvaluator {
  def main(args: Array[String]) {
    val storage: TranslationPairStorage = new PostgresFirstLetterStorage()

    //storage.initialize(
    //  scala.io.Source.fromFile("/Users/jodaiber/Desktop/src/FilmTit/dbms_experiments/corpus.1m.txt").getLines().map(
    //    line => { new TranslationPair(line.trim(), null) }
    //  ).filter(pair => pair.sourceSentence != "" && pair.sourceSentence.split(" ").size < 30)
    //)

    storage.candidates("Aamazon is a retailer.")
  }
}