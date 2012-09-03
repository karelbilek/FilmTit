/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.core.search.postgres

import cz.filmtit.core.model._
import collection.mutable.ListBuffer
import java.sql.Connection
import storage.{Signature, SignatureTranslationPairStorage}
import org.postgresql.util.PSQLException
import cz.filmtit.share.exceptions.DatabaseException
import scala.collection.JavaConversions._
import cz.filmtit.core.Configuration
import cz.filmtit.share._

/**
 * Base class for all signature based translation pair storages
 * using Postgres.
 *
 * @author Joachim Daiber
 */

abstract class BaseSignatureStorage(
  l1: Language,
  l2: Language,
  source: TranslationSource,
  signatureTable: String,
  connection: Connection,
  useInMemoryDB: Boolean = false,
  reversible: Boolean = false,
  maximumSignatureLength: Int = 500
) extends BaseStorage(l1, l2, source, connection, useInMemoryDB)
with SignatureTranslationPairStorage {

  override def warmup() {
    //connection.createStatement().execute(
    //      "SELECT * FROM %s ORDER BY SIGNATURE_l1;".format(signatureTable)
    //)
  }


  /**Write the signatures for the chunk table to the database. */
  override def reindex() {

    val FETCH_SIZE = 100000

    connection.createStatement().execute(
      "DROP TABLE IF EXISTS %s;".format(signatureTable)
    )
    connection.createStatement().execute(
       if (!reversible)
        (("CREATE TABLE %s (pair_id INTEGER PRIMARY KEY , signature_l1 %s, signature_l2 %s, " +
            "FOREIGN KEY (pair_id) REFERENCES %s (pair_id));")
          .format(signatureTable, textDataType, textDataType, pairTable)     )
       else
        ("CREATE TABLE %s (pair_id INTEGER PRIMARY KEY, " +
          "signature_l1 %s, annotations_l1 %s, " +
          "signature_l2 %s, annotations_l2 %s, FOREIGN KEY (pair_id) REFERENCES %s (pair_id));")
        .format(signatureTable,textDataType, textDataType, textDataType, textDataType, pairTable)
    )


    if (!useInMemoryDB)
      connection.setAutoCommit(false)

    log.info("Reading chunks...")
    val selStmt = connection.createStatement(
      java.sql.ResultSet.TYPE_FORWARD_ONLY,
      java.sql.ResultSet.CONCUR_READ_ONLY
    )

    selStmt.setFetchSize(FETCH_SIZE)
    selStmt.execute("SELECT * FROM %s ORDER BY pair_count DESC;".format(pairTable))

    log.info("Creating chunk signatures...")

    val inStmt = connection.prepareStatement(

      (if (!reversible)
        "INSERT INTO %s(pair_id, signature_l1, signature_l2) VALUES(?, ?, ?);"
      else
        "INSERT INTO %s(pair_id, signature_l1, annotations_l1, " +
          "signature_l2, annotations_l2) VALUES(?, ?, ?, ?, ?);"
        ).format(signatureTable)
    )
    inStmt.setFetchSize(FETCH_SIZE)

    var i = 0

    val resultSet = selStmt.getResultSet

    while (resultSet.next()) {

      val row = resultSet

      if (row != null) {

        val c1 = new Chunk(row.getString("chunk_l1"))
        if (this.requiresTokenization && tm != null)
          tm.tokenize(c1, l1)
        val sigL1 = signature(c1, l1)

        val c2 = new Chunk(row.getString("chunk_l2"))
        if (this.requiresTokenization && tm != null)
          tm.tokenize(c2, l2)
        val sigL2 = signature(c2, l2)

        if(sigL1.surfaceform.size < maximumSignatureLength && sigL2.surfaceform.size < maximumSignatureLength) {

          if (reversible) {
            inStmt.setInt(1, row.getInt("pair_id"))

            //Signature, annotations for L1
            inStmt.setString(2, sigL1.surfaceform)
            inStmt.setString(3, sigL1.listAnnotations())

            //Signature, annotations for L2
            inStmt.setString(4, sigL2.surfaceform)
            inStmt.setString(5, sigL2.listAnnotations())

          }else{
            inStmt.setInt(1, row.getInt("pair_id"))
            inStmt.setString(2, sigL1.surfaceform)
            inStmt.setString(3, sigL2.surfaceform)
          }

          inStmt.execute()

          i += 1
          if(i % 50000 == 0)
            log.info("Wrote %d signatures...".format(i))
        }
      }
    }

    if (!useInMemoryDB)
      connection.commit()

    inStmt.close()
    selStmt.close()

    //Create the index:
    log.info("Creating indexes...")

    connection.createStatement().execute(
      "CREATE INDEX idx_chunkl1_%s ON %s (signature_l1);"
        .format(signatureTable, signatureTable)
    )

    connection.createStatement().execute(
      "CREATE INDEX idx_chunkl2_%s ON %s (signature_l2);"
        .format(signatureTable, signatureTable)
    )

    connection.commit()
  }


  /**
   * If the storage is reversible, add annotations to the chunk that represent
   * parts of the chunk that are special, e.g. named entities to be post-edited.
   */
  def annotate(chunk: Chunk, signature: Signature) {
    //do nothing, must be overridden
  }


  /**
   * This is the candidate retrieval method all signature-based storages use.
   * All signature-based storages differ only in the implementation of the
   * signature method, which is used for retrieval here and for indexing
   * in the this class.
   *
   * @param chunk chunk to be searched
   * @param language language of the chunk
   * @return
   */
  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val sigColumn = if(language.equals(l1)) "signature_l1" else "signature_l2"
    val select = connection.prepareStatement("SELECT * FROM %s AS sigs LEFT JOIN %s AS chunks ON sigs.pair_id = chunks.pair_id WHERE sigs.%s = ? LIMIT %s;".format(signatureTable, pairTable, sigColumn, maxCandidates))
    val mediaSourceSelect = connection.prepareStatement("SELECT * FROM %s as mapping LEFT JOIN %s AS ms ON mapping.source_id = ms.source_id WHERE pair_id = ?;".format(chunkSourceMappingTable, mediasourceTable))

    //Use the signature function of the specific storage:
    select.setString(1, signature(chunk, language).surfaceform)

    //Get and process all candidates
    val rs = try {
      select.executeQuery()
    } catch {
      case e: PSQLException => {
        e.printStackTrace()
        throw new DatabaseException("Could not use database indexes, please make sure the TM is properly indexed. To reindex, run $ make index in the main directory.")
      }
    }

    val candidates = new ListBuffer[TranslationPair]()
    while (rs.next()) {

      val chunkL1 = if(language.equals(l1)) new Chunk(rs.getString("chunk_l1")) else new Chunk(rs.getString("chunk_l2"))
      val chunkL2 = if(language.equals(l1)) new Chunk(rs.getString("chunk_l2")) else new Chunk(rs.getString("chunk_l1"))

      val count: Int = rs.getString("pair_count").toInt
      val pairID: Long = rs.getLong("pair_id")

      //Restore the signature for both chunks if possible
      val sigL1 = if (!reversible)
        Signature.fromString(rs.getString("signature_l1"))
      else
        Signature.fromDatabase(rs.getString("signature_l1"), rs.getString("annotations_l1"))

      val sigL2 = if (!reversible)
        Signature.fromString(rs.getString("signature_l2"))
      else
        Signature.fromDatabase(rs.getString("signature_l2"), rs.getString("annotations_l2"))

      if (language == l1)
        annotate(chunkL1, sigL1)
      else
        annotate(chunkL2, sigL2)

      //Add all media sources to the translation pair
      mediaSourceSelect.setLong(1, pairID)
      mediaSourceSelect.execute()

      val mediaSources = new java.util.ArrayList[MediaSource]()
      while(mediaSourceSelect.getResultSet.next()) {
        new MediaSource(
          mediaSourceSelect.getResultSet.getString("title"),
          mediaSourceSelect.getResultSet.getString("year"),
          mediaSourceSelect.getResultSet.getString("genres")
        )
      }

      //Add the candidate to the list of candidates
      val tp = new TranslationPair(
                chunkL1,
                chunkL2,
                source,
                mediaSources
              )
      tp.setCount(count)
      tp.setId(pairID)
      candidates += tp
    }

    candidates.toList
  }

  def requiresTokenization = true
}
