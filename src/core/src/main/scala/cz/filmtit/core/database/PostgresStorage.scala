package cz.filmtit.core.database

import cz.filmtit.core.model.{TranslationPairStorage, MediaSource, TranslationPair}
import org.postgresql.util.PSQLException
import java.sql.{SQLException, PreparedStatement, Statement, DriverManager}
import java.net.ConnectException
import cz.filmtit.core.model.Language._


/**
 * @author Joachim Daiber
 *
 */

abstract class PostgresStorage(l1: Language, l2: Language) extends TranslationPairStorage(l1, l2) {

  //Load the driver:
  classOf[org.postgresql.Driver]

  //Initialize connection
  val connection = try {
    DriverManager.getConnection("jdbc:postgresql://localhost/filmtit", "postgres", "postgres")
  } catch{
    case e: PSQLException => throw new ConnectException("Could not connect to database. " +
      "Please check if the database is running.")
  }

  var tableNameChunks = "chunks"
  var tableNameMediaSources = "mediasources"

  var maxCandidates = 500


  def initialize() {
    connection.createStatement().execute("DROP TABLE IF EXISTS %s CASCADE; CREATE TABLE %s (source_id SERIAL PRIMARY KEY, title TEXT, year VARCHAR(4), genres TEXT);".format(tableNameMediaSources, tableNameMediaSources))
    connection.createStatement().execute("DROP TABLE IF EXISTS %s; CREATE TABLE %s (chunk_id SERIAL PRIMARY KEY, chunk_l1 TEXT, chunk_l2 TEXT, source_id INTEGER references %s(source_id));".format(tableNameChunks, tableNameChunks, tableNameMediaSources))
  }

  def createChunkTables(translationPairs: TraversableOnce[TranslationPair]) {

    val insertStatement = connection.prepareStatement("INSERT INTO %s (chunk_l1, chunk_l2, source_id) VALUES (?, ?, ?)".format(tableNameChunks))

    System.err.println("Reading aligned chunks...")
    translationPairs foreach {
      translationPair => {
        try {
          insertStatement.setString(1, translationPair.source)
          insertStatement.setString(2, translationPair.target)
          insertStatement.setLong(3, translationPair.mediaSource.id)
          insertStatement.execute()
        } catch {
          case e: SQLException => {
            System.err.println("Could not write pair to database: " + translationPair)
            e.printStackTrace()
          }
        }
      }
    }

  }


  def getMediaSource(id: Int): MediaSource = {
    val statement: PreparedStatement = connection.prepareStatement("SELECT * FROM %s where source_id = ? LIMIT 1;".format(tableNameMediaSources))
    statement.setLong(1, id)
    statement.execute()
    statement.getResultSet.next()

    new MediaSource(statement.getResultSet.getString("title"), statement.getResultSet.getString("year"), statement.getResultSet.getString("year"))
  }

  def addMediaSource(mediaSource: MediaSource): Long = {
    val statement = connection.prepareStatement("INSERT INTO %s(title, year, genres) VALUES(?, ?, ?) RETURNING source_id;".format(tableNameMediaSources))
    statement.setString(1, mediaSource.title)
    statement.setString(2, mediaSource.year)
    statement.setString(3, mediaSource.genres mkString ",")
    statement.execute()
    statement.getResultSet.next()
    statement.getResultSet.getLong("source_id")
  }

}

