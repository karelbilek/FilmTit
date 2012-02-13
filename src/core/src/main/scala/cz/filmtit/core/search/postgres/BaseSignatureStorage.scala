package cz.filmtit.core.search.postgres

import cz.filmtit.core.model._
import collection.mutable.ListBuffer
import cz.filmtit.core.model.Language
import storage.{Signature, SignatureTranslationPairStorage}
import data.{Chunk, TranslationPair}


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
  reversible: Boolean = false
) extends BaseStorage(l1, l2, source)
  with SignatureTranslationPairStorage {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
    createChunks(translationPairs)
    reindex()
  }

  /**Write the signatures for the chunk table to the database. */
  override def reindex() {
    connection.createStatement().execute(
      "DROP TABLE IF EXISTS %s;".format(signatureTable)
    )
    connection.createStatement().execute(
      ( if (!reversible)
         "CREATE TABLE %s (chunk_id INTEGER PRIMARY KEY references %s(chunk_id), signature_l1 TEXT, signature_l2 TEXT);"
        else
        "CREATE TABLE %s (chunk_id INTEGER PRIMARY KEY references %s" +
          "(chunk_id), signature_l1 TEXT, annotations_l1 TEXT, " +
          "signature_l2 TEXT, annotations_l2 TEXT);"
       ).format(signatureTable, chunkTable)
    )

    connection.setAutoCommit(false);

    log.info("Reading chunks...")
    val selStmt = connection.createStatement(
      java.sql.ResultSet.TYPE_FORWARD_ONLY,
      java.sql.ResultSet.CONCUR_READ_ONLY
    )

    selStmt.setFetchSize(1000);
    selStmt.execute("SELECT * FROM %s;".format(chunkTable))

    log.info("Creating chunk signatures...")

    val inStmt = connection.prepareStatement(

      (if (!reversible)
        "INSERT INTO %s(chunk_id, signature_l1, signature_l2) VALUES(?, ?, ?);"
      else
        "INSERT INTO %s(chunk_id, signature_l1, annotations_l1, " +
          "signature_l2, annotations_l2) VALUES(?, ?, ?, ?, ?);"
      ).format(signatureTable)
    )
    //inStmt.setFetchSize(500)

    var i = 0
    while (selStmt.getResultSet.next()) {
      val row = selStmt.getResultSet

      val sigL1 = signature(row.getString("chunk_l1"), l1)
      val sigL2 = signature(row.getString("chunk_l2"), l2)

      
      if (reversible) {
        inStmt.setInt(1, row.getInt("chunk_id"))

        //Signature, annotations for L1
        inStmt.setString(2, sigL1.surfaceform)
        inStmt.setString(3, sigL1.listAnnotations())

        //Signature, annotations for L2
        inStmt.setString(4, sigL2.surfaceform)
        inStmt.setString(5, sigL2.listAnnotations())

      }else{
        inStmt.setInt(1, row.getInt("chunk_id"))
        inStmt.setString(2, sigL1.surfaceform)
        inStmt.setString(3, sigL2.surfaceform)
      }

      inStmt.execute()

      i += 1
      if(i % 50000 == 0)
        log.info("Wrote %d signatures...".format(i))
    }

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
   * If the storage is reversible,a dd annotations to the chunk that represent
   * parts of the chunk that are special, e.g. that should be post-edited.
   */
  def annotate(chunk: Chunk, signature: Signature) {}


  override def addTranslationPair(translationPair: TranslationPair) = {}


  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {

    val select = connection.prepareStatement("SELECT * FROM %s AS sigs LEFT JOIN %s AS chunks ON sigs.chunk_id = chunks.chunk_id WHERE sigs.signature_l1 = ? LIMIT %d;".format(signatureTable, chunkTable, maxCandidates))
    select.setString(1, signature(chunk, language).surfaceform)
    val rs = select.executeQuery()

    val candidates = new ListBuffer[TranslationPair]()
    while (rs.next()) {

      val chunkL1: Chunk = rs.getString("chunk_l1")
      val chunkL2: Chunk = rs.getString("chunk_l2")

      val sigL1 = if (!reversible)
        Signature.fromString(rs.getString("signature_l1"))
      else
        Signature.fromDatabase(rs.getString("signature_l1"), rs.getString("annotations_l1"))
      
      val sigL2 = if (!reversible)
        Signature.fromString(rs.getString("signature_l2"))
      else
        Signature.fromDatabase(rs.getString("signature_l2"), rs.getString("annotations_l2"))


      /*
        If there are any annotations added by the signature method, e.g.
        by a NER, add them to the TP.
       */
      if (language == l1)
        annotate(chunkL1, sigL1)
      else
        annotate(chunkL2, sigL2)


      candidates += new TranslationPair(
        chunkL1,
        chunkL2,
        source,
        getMediaSource(rs.getInt("source_id"))
      )
    }

    candidates.toList
  }

}
