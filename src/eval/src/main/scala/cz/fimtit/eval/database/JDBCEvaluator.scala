package cz.fimtit.eval.database

import java.sql.{Statement, DriverManager}


/**
 * @author Joachim Daiber
 *
 */

class JDBCEvaluator {

  //Load the driver:
  classOf[org.postgresql.Driver]

  //Initialize
  val connection = DriverManager.getConnection("jdbc:postgresql://localhost/filmtit", "postgres", "postgres")

  def create() {
    connection.createStatement().execute("DROP TABLE IF EXISTS sentences; CREATE TABLE sentences (sentence VARCHAR(10000));")

    val insertStatement = connection.prepareStatement("INSERT INTO sentences (sentence) VALUES (?)")

    println("Reading sentences...")
    scala.io.Source.fromFile("/Users/jodaiber/Desktop/src/FilmTit/dbms_experiments/corpus.1m.txt").getLines().foreach(
      line => {
        insertStatement.setString(1, line.trim())
        insertStatement.execute()
      }
    )
    //insertStatement.executeBatch()

    //Create the index:
    println("Creating index...")
    connection.createStatement().execute("CREATE INDEX idx_sentences ON sentences USING gist (sentence gist_trgm_ops);")

  }

  def query(sentence: String) {
    val select = connection.prepareStatement("SELECT sentence, similarity(sentence, ?) AS sml FROM sentences WHERE sentence % ? ORDER BY sml DESC, sentence;")
    select.setString(1, sentence)
    select.setString(2, sentence)
    val rs = select.executeQuery()

    while (rs.next) {
      println(rs.getFloat("sml") + ": " + rs.getString("sentence"))
    }
  }

}

object JDBCEvaluator {
  def main(args: Array[String]) {
    new JDBCEvaluator().create()
    //new JDBCEvaluator().query("Germany is a state")
  }
}