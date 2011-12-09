package cz.fimtit.eval.database

import cz.filmtit.core.model._


/**
 * @author Joachim Daiber
 *
 */

class PostgresFirstLetterStorage extends PostgresSignatureBasedStorage {

  def indexName = "idx_firstletter"

  /**
   * Use the lowercased first letter of each word in the sentence as the signature.
   */
  override def signature(chunk: Chunk): String = {
    new String(chunk.surfaceform.split(" ") filter (_ != "") map { _.charAt(0).toLower })
  }

  override def name: String = "Translation pair storage using the first letter of every word as an index."

}
