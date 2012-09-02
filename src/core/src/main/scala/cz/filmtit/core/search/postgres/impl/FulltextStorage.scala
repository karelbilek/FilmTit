package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseStorage
import java.sql.Connection
import cz.filmtit.share.{Language, TranslationPair, TranslationSource, Chunk}
import cz.filmtit.core.Configuration


/**
 * Postgres-based storage using a full-text index.
 *
 * @author Joachim Daiber
 */
class FulltextStorage(
  l1: Language,
  l2: Language,
  connection: Connection,
  useInMemoryDB: Boolean = false
) extends BaseStorage(
  l1,
  l2,
  TranslationSource.INTERNAL_FUZZY,
  connection,
  useInMemoryDB
) {

  override def warmup() {}

  /**
   * Chunks that should be queried with this searcher must have at least this number of content words.
   * This is tested by creating the vector for the chunk and then testing if the size of the query
   * is >= MIN_QUERY_LENGTH.
   */
  val MIN_QUERY_LENGTH = 2

  override def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    val searchColumn = if(language.equals(l1)) "chunk_l1" else "chunk_l2"
    val l = language.getName.toLowerCase
    val select = connection.prepareStatement("SELECT pair_id, chunk_l1, chunk_l2, pair_count, ts_rank(to_tsvector('"+l+"', "+searchColumn+"), query, 32 /* rank/(rank+1) */) AS rank FROM "+pairTable+", plainto_tsquery('"+l+"', ?) query WHERE query @@ to_tsvector('"+l+"', "+searchColumn+") AND numnode(query) >= "+MIN_QUERY_LENGTH+" ORDER BY rank DESC LIMIT "+this.maxCandidates+";")
    select.setString(1, chunk.getSurfaceForm)

    val rs = select.executeQuery()
    var tps = List[TranslationPair]()

    while (rs.next) {
      val c1 = if(language.equals(l1)) new Chunk(rs.getString("chunk_l1")) else new Chunk(rs.getString("chunk_l2"))
      val c2 = if(language.equals(l1)) new Chunk(rs.getString("chunk_l2")) else new Chunk(rs.getString("chunk_l1"))
      val tp = new TranslationPair(c1, c2, TranslationSource.INTERNAL_FUZZY)
      tp.setId(rs.getInt("pair_id"))
      tp.setScore(rs.getDouble("rank"))
      tp.setCount(rs.getInt("pair_count"))
      tps ::= tp
    }

    tps
  }

  override def name: String = "Translation pair storage using a full text index."

  def reindex() {
    log.info("Reindexing...")
    connection.createStatement().execute(("DROP index IF EXISTS idx_fulltext_l1;" +
      "CREATE INDEX idx_fulltext_l1 ON %s USING gin(to_tsvector('%s', chunk_l1));").format(pairTable, l1.getName.toLowerCase))
    connection.createStatement().execute(("DROP index IF EXISTS idx_fulltext_l2;" +
      "CREATE INDEX idx_fulltext_l2 ON %s USING gin(to_tsvector('%s', chunk_l2));").format(pairTable, l2.getName.toLowerCase))
  }

  def requiresTokenization = false
}
