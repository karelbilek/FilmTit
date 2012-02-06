package cz.filmtit.core.database.postgres

import cz.filmtit.core.Configuration
import cz.filmtit.core.model.storage.TranslationPairStorage
import org.postgresql.util.PSQLException
import java.sql.{SQLException, DriverManager}
import java.net.ConnectException
import com.weiglewilczek.slf4s.Logger
import cz.filmtit.core.model.data.{TranslationPair, MediaSource}
import cz.filmtit.core.model.Language._


/**
 * @author Joachim Daiber
 *
 */

abstract class BaseStorage(l1: Language, l2: Language)
  extends TranslationPairStorage(l1, l2) {

  val log = Logger(this.getClass.getSimpleName)

  //Load the driver:
  classOf[org.postgresql.Driver]

  //Initialize connection
  val connection = try {
    DriverManager.getConnection(Configuration.dbConnector,
      Configuration.dbUser,
      Configuration.dbPassword)
  } catch {
    case e: PSQLException => throw new ConnectException(
      "Could not connect to database. " +
        "Please check if the database is running.")
  }

  var chunkTable = "chunks"
  var mediasourceTable = "mediasources"

  var maxCandidates = 500


  def createChunks(translationPairs: TraversableOnce[TranslationPair]) {

    connection.createStatement().execute((
      "DROP TABLE IF EXISTS %s; " +
        "CREATE TABLE %s (chunk_id SERIAL PRIMARY KEY, chunk_l1 TEXT, chunk_l2 TEXT, source_id INTEGER references %s(source_id));"
      ).format(chunkTable, chunkTable, mediasourceTable))

    connection.createStatement().execute((
      "DROP TABLE IF EXISTS %s CASCADE; " +
        "CREATE TABLE %s (source_id SERIAL PRIMARY KEY, title TEXT, year VARCHAR(4), genres TEXT);"
      ).format(mediasourceTable, mediasourceTable))

    val inStmt = connection.prepareStatement("INSERT INTO %s (chunk_l1, chunk_l2, source_id) VALUES (?, ?, ?)".format(chunkTable))

    System.err.println("Reading aligned chunks...")
    translationPairs foreach {
      translationPair => {
        try {
          inStmt.setString(1, translationPair.source)
          inStmt.setString(2, translationPair.target)
          inStmt.setLong(3, translationPair.mediaSource.id)
          inStmt.execute()
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
    val stmt = connection.prepareStatement("SELECT * FROM %s where source_id = ? LIMIT 1;".format(mediasourceTable))
    stmt.setLong(1, id)
    stmt.execute()
    stmt.getResultSet.next()

    new MediaSource(stmt.getResultSet.getString("title"), stmt.getResultSet.getString("year"), stmt.getResultSet.getString("year"))
  }

  def addMediaSource(mediaSource: MediaSource): Long = {
    val stmt = connection.prepareStatement("INSERT INTO %s(title, year, genres) VALUES(?, ?, ?) RETURNING source_id;".format(mediasourceTable))

    stmt.setString(1, mediaSource.title)
    stmt.setString(2, mediaSource.year)
    stmt.setString(3, mediaSource.genres mkString ",")
    stmt.execute()

    stmt.getResultSet.next()
    stmt.getResultSet.getLong("source_id")
  }

}

