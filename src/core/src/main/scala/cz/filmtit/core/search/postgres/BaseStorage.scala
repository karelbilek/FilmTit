package cz.filmtit.core.search.postgres

import cz.filmtit.core.Configuration
import org.postgresql.util.PSQLException
import com.weiglewilczek.slf4s.Logger
import cz.filmtit.core.model.storage.{MediaStorage, TranslationPairStorage}
import java.sql.{SQLException, DriverManager}
import collection.mutable.HashSet
import gnu.trove.map.hash.TObjectLongHashMap
import scala.collection.JavaConversions._
import cz.filmtit.share.{Language, TranslationPair, MediaSource, TranslationSource}


/**
 * Base class for all translation pair storages using Postgres.
 *
 * @author Joachim Daiber
 *
 */

abstract class BaseStorage(
  l1: Language,
  l2: Language,
  source: TranslationSource,
  readOnly: Boolean = true
  ) extends TranslationPairStorage(l1, l2)
with MediaStorage {

  val log = Logger(this.getClass.getSimpleName)

  //Load the driver:
  classOf[org.postgresql.Driver]

  //Initialize connection
  val connection = try {
    DriverManager.getConnection(Configuration.dbConnector,
      Configuration.dbUser,
      Configuration.dbPassword)
  } catch {
    case e: PSQLException => {
      System.err.println("Could not connect to database %s. Please check if the DBMS is running and database exists.".format(Configuration.dbConnector))
      System.exit(1)
      null
    }
  }

  //Assure the database is in read-only mode if required.
  if (readOnly == true)
    connection.setReadOnly(true)

  var pairTable = "chunks"
  var chunkSourceMappingTable = "chunk_mediasources"
  var mediasourceTable = "mediasources"

  var maxCandidates = 200

  /**
   * Drop all tables from the database and recreate them.
   */
  def reset() {
    System.err.println("Resetting BaseStorage (chunks, mediasources).")

    connection.createStatement().execute(
      "DROP TABLE IF EXISTS %s CASCADE; DROP TABLE IF EXISTS %s CASCADE; DROP TABLE IF EXISTS %s CASCADE;"
        .format(pairTable, mediasourceTable, chunkSourceMappingTable))

    connection.createStatement().execute(
      "CREATE TABLE %s (source_id SERIAL PRIMARY KEY, title TEXT, year VARCHAR(4), genres TEXT);"
        .format(mediasourceTable))

    connection.createStatement().execute(
      ("CREATE TABLE %s (pair_id SERIAL PRIMARY KEY, chunk_l1 TEXT, chunk_l2 TEXT, count INTEGER);")
        .format(pairTable))

    connection.createStatement().execute(
      ("CREATE TABLE %s (" +
        "pair_id INTEGER references %s(pair_id), " +
        "source_id INTEGER references %s(source_id)," +
        "PRIMARY KEY(pair_id, source_id)" +
        ");")
        .format(chunkSourceMappingTable, pairTable, mediasourceTable))

  }


  /**
   * The following two data structures are only used for indexing, hence
   * they are lazy and are not initialized in read-only mode.
   */
  private lazy val pairIDCache: TObjectLongHashMap[java.lang.String] = new TObjectLongHashMap[java.lang.String]()
  private lazy val pairMediaSourceMappings: HashSet[Pair[Long, Long]] = HashSet[Pair[Long, Long]]()

  private lazy val msInsertStmt = connection.prepareStatement("INSERT INTO %s(pair_id, source_id) VALUES(?, ?);".format(chunkSourceMappingTable))
  pairMediaSourceMappings.clear()
  /**
   * Add a media source <-> translation pair correspondence to
   * the database.
   *
   * @param pairID pair identifier that will be linked to the media source
   * @param mediaSourceID media source DB identifier
   */
  private def addMediaSourceForChunk(pairID: Long, mediaSourceID: Long) {

    if ( !(pairMediaSourceMappings.contains(Pair(pairID, mediaSourceID))) ) {
      msInsertStmt.setLong(1, pairID)
      msInsertStmt.setLong(2, mediaSourceID)
      msInsertStmt.execute()
      pairMediaSourceMappings.add(Pair(pairID, mediaSourceID))
    }

  }

  /**
   * Search for the translation pair in the database and return its
   * ID if it is present.
   *
   * @param translationPair the translation pair to be looked up
   * @return
   */
  private def pairIDInDatabase(translationPair: TranslationPair): Option[Long] = {
    val l: Long = pairIDCache.get("%s-%s".format(translationPair.getChunkL1, translationPair.getChunkL2))
    if (l == 0) {
      None
    }else{
      Some(l)
    }
  }


  /**
   * This is the only place where {TranslationPair}s are actually
   * added to the database. All subclasses of BaseStorage work with the
   * translation pairs that were added to the database by this method.
   *
   * @param translationPairs a Traversable of translation pairs
   */
  def addVerbose(translationPairs: TraversableOnce[TranslationPair], autoCommit: Boolean = false) {

    val inStmt = connection.prepareStatement(("INSERT INTO %s (chunk_l1, chunk_l2, count) VALUES (?, ?, 1) RETURNING pair_id;").format(pairTable))
    val upStmt = connection.prepareStatement(("UPDATE %s SET count = count + 1 WHERE pair_id = ?;").format(pairTable))

    //Important for performance: Only commit after all INSERT statements are
    //executed:
    connection.setAutoCommit(autoCommit)

    System.err.println("Writing translation pairs to database...")
    translationPairs foreach { translationPair => {
      try {

        val pairID = pairIDInDatabase(translationPair) match {

          //Normal case: there is no equivalent translation pair in the database
          case None => {

            inStmt.setString(1, translationPair.getChunkL1.getSurfaceform)
            inStmt.setString(2, translationPair.getChunkL2.getSurfaceform)
            inStmt.execute()

            //Get the pair_id of the new translation pair
            inStmt.getResultSet.next()
            val newPairID = inStmt.getResultSet.getLong("pair_id")

            //Remember that we already put it into the database
            pairIDCache.put("%s-%s".format(translationPair.getChunkL1, translationPair.getChunkL2), newPairID)

            newPairID
          }

          //It is already there
          case Some(existingPairID) => {
            //Special case: there is an equivalent translation pair in the database,
            upStmt.setLong(1, existingPairID)
            upStmt.execute()
            existingPairID
          }
        }

        //Add the MediaSource as the source to the TP
        addMediaSourceForChunk(pairID, translationPair.getMediaSource.getId)
      } catch {
        case e: SQLException => {
          if (!autoCommit) {
            System.err.println("Database error in current batch, switching to auto-commit mode. ");
            addVerbose(translationPairs, autoCommit=true)
            return;
          } else {
            System.err.println("Single insert failed, skipping it..." + translationPair);
            e.printStackTrace()
          }

        }
      }
    }
  }

    System.err.println("Clearing pair<->MS set.");
    pairMediaSourceMappings.clear()

    //Commit the changes to the database:
    if (!autoCommit)
      connection.commit()
  }

 /**
   * This is the only place where {TranslationPair}s are actually
   * added to the database. All subclasses of BaseStorage work with the
   * translation pairs that were added to the database by this method.
   *
   * @param translationPairs a Traversable of translation pairs
   */
  def add(translationPairs: TraversableOnce[TranslationPair]) {
    addVerbose(translationPairs)
  }



  /**
    * Get a media source by its database identifier.
    * @param id media source identifier
    * @return
    */
  def getMediaSource(id: Int): MediaSource = {
    val stmt = connection.prepareStatement("SELECT * FROM %s where source_id = ? LIMIT 1;".format(mediasourceTable))
    stmt.setLong(1, id)
    stmt.execute()
    stmt.getResultSet.next()

    new MediaSource(
      stmt.getResultSet.getString("title"),
      stmt.getResultSet.getString("year"),
      stmt.getResultSet.getString("genres")
    )
  }


  /**
   * Add a media source to the database.
   * @param mediaSource filled media source object
   * @return
   */
  def addMediaSource(mediaSource: MediaSource): Long = {
    val stmt = connection.prepareStatement("INSERT INTO %s(title, year, genres) VALUES(?, ?, ?) RETURNING source_id;".format(mediasourceTable))

    stmt.setString(1, mediaSource.getTitle)
    stmt.setString(2, mediaSource.getYear)
    stmt.setString(3, mediaSource.getGenres mkString ",")
    stmt.execute()

    stmt.getResultSet.next()
    stmt.getResultSet.getLong("source_id")
  }

}

