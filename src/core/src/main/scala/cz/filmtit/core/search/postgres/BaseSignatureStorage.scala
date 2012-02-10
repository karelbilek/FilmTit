package cz.filmtit.core.search.postgres

import cz.filmtit.core.model._
import collection.mutable.ListBuffer
import cz.filmtit.core.model.Language
import data.{Chunk, TranslationPair}
import storage.SignatureBasedStorage


/**
 * @author Joachim Daiber
 *
 */

abstract class BaseSignatureStorage(l1: Language, l2: Language,
                                    signatureTable: String)
  extends BaseStorage(l1, l2)
  with SignatureBasedStorage {

  override def initialize(translationPairs: TraversableOnce[TranslationPair]) {
    createChunks(translationPairs)
    reindex()
  }

  override def reindex() {
    connection.createStatement().execute("DROP TABLE IF EXISTS %s; CREATE TABLE %s (chunk_id INTEGER PRIMARY KEY references %s(chunk_id), signature_l1 TEXT, signature_l2 TEXT);".format(signatureTable, signatureTable, chunkTable))

    connection.setAutoCommit(false);

    log.info("Reading chunks...")
    val selStmt = connection.createStatement(
      java.sql.ResultSet.TYPE_FORWARD_ONLY,
      java.sql.ResultSet.CONCUR_READ_ONLY
    )

    selStmt.setFetchSize(1000);
    selStmt.execute("SELECT * FROM %s LIMIT 500000;".format(chunkTable))

    log.info("Creating chunk signatures...")
    val inStmt = connection.prepareStatement(
      "INSERT INTO %s(chunk_id, signature_l1, signature_l2) VALUES(?, ?, ?);"
        .format(signatureTable)
    )
    //inStmt.setFetchSize(500)

    var i = 0
    while (selStmt.getResultSet.next()) {
      val row = selStmt.getResultSet

      inStmt.setInt(1, row.getInt("chunk_id"))
      inStmt.setString(2, signature(row.getString("chunk_l1"), l1))
      inStmt.setString(3, signature(row.getString("chunk_l2"), l2))

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
   * Add annotations to the chunk that represent parts of the chunk
   * that are special, e.g. that should be edited.
   */
  def annotate(chunk: Chunk, signature: String): Chunk = chunk
  
  
  override def addTranslationPair(translationPair: TranslationPair) = {}

  
  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {

    val select = connection.prepareStatement("SELECT * FROM %s AS sigs LEFT JOIN %s AS chunks ON sigs.chunk_id = chunks.chunk_id WHERE sigs.signature_l1 = ? LIMIT %d;".format(signatureTable, chunkTable, maxCandidates))
    select.setString(1, signature(chunk, language))
    val rs = select.executeQuery()

    val candidates = new ListBuffer[TranslationPair]()
    while (rs.next()) {

      val chunkL1: Chunk = rs.getString("chunk_l1")
      val chunkL2: Chunk = rs.getString("chunk_l2")

      /*
        If there are any annotations added by the signature method, e.g.
        by a NER, add them to the TP.
       */
      language match {
        case l if (l equals l1) => annotate(chunkL1, rs.getString("signature_l1"))
        case l if (l equals l2) => annotate(chunkL2, rs.getString("signature_l2"))
      }

      candidates += new TranslationPair(
        chunkL1,
        chunkL2,
        getMediaSource(rs.getInt("source_id"))
      )
    }

    candidates.toList
  }

  //abstract def signature(sentence: Chunk, language: Language): String

}
