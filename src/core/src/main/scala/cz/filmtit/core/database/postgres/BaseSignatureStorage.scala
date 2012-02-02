package cz.filmtit.core.database.postgres

import cz.filmtit.core.model._
import java.sql.{PreparedStatement, Statement}
import collection.mutable.ListBuffer
import cz.filmtit.core.model.Language._


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

    val pairs: Statement = connection.createStatement()
    pairs.execute("SELECT * FROM %s;".format(chunkTable))

    val inStmt: PreparedStatement = connection.prepareStatement("INSERT INTO chunk_signatures(chunk_id, signature_l1, signature_l2) VALUES(?, ?, ?);")
    while (pairs.getResultSet.next()) {
      val row = pairs.getResultSet

      inStmt.setInt(1, row.getInt("chunk_id"))
      inStmt.setString(2, signature(row.getString("chunk_l1"), l1))
      inStmt.setString(3, signature(row.getString("chunk_l2"), l2))

      inStmt.execute()
    }

    //Create the index:
    println("Creating indexes...")
    connection.createStatement().execute(
      "CREATE INDEX idx_chunkl1_%s ON %s (signature_l1);"
        .format(signatureTable, signatureTable))

    connection.createStatement().execute((
      "CREATE INDEX idx_chunkl2_%s ON %s (signature_l2);")
      .format(signatureTable, signatureTable))

  }

  override def addTranslationPair(translationPair: TranslationPair) = {}

  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {

    val select = connection.prepareStatement("SELECT * FROM %s AS sigs LEFT JOIN %s AS chunks ON sigs.chunk_id = chunks.chunk_id WHERE sigs.signature_l1 = ? LIMIT %d;".format(signatureTable, chunkTable, maxCandidates))
    select.setString(1, signature(chunk, language))
    val rs = select.executeQuery()

    val candidates = new ListBuffer[TranslationPair]()
    while (rs.next()) {
      candidates += new TranslationPair(rs.getString("chunk_l1"), rs.getString("chunk_l2"), getMediaSource(rs.getInt("source_id")))
    }

    candidates.toList
  }

  def signature(sentence: Chunk, language: Language): String

}
