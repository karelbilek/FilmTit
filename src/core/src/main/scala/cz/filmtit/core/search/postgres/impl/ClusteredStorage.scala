package cz.filmtit.core.search.postgres.impl

import java.sql.Connection
import cz.filmtit.core.search.postgres.BaseSignatureStorage
import scala.collection.mutable.HashMap
import scala.io.Source
import cz.filmtit.core.model.storage.Signature
import cz.filmtit.share._
import cz.filmtit.core.Configuration


/**
 * @author Joachim Daiber
 *
 */

class ClusteredStorage(
  l1: Language,
  l2: Language,
  connection: Connection,
  hssql: Boolean = false
) extends
BaseSignatureStorage(
  l1,
  l2,
  TranslationSource.INTERNAL_FUZZY,
  "sign_clustered",
  connection,
  hssql
) {

  val cluster: HashMap[String, Int] = new HashMap[String, Int]()

  /*
   * Read in the result of the clustering, where each line looks like this:
   * {token}\t{cluster number}
   *
   * This clustering can be produced using mkcls (http://www-i6.informatik.rwth-aachen.de/web/Software/mkcls.html):
   * $ ./mkcls -c1500 -n10 -pcorpus.1m.txt -Vout
   */
  Source.fromFile("/Users/jodaiber/Desktop/out").getLines().foreach({
    line: String => {
      val Array(token, clusterID) = line.trim().split("\t")
      cluster.put(token, clusterID.toInt)
    }
  })

  override def signature(chunk: Chunk, language: Language): Signature = {
    new String(chunk.getSurfaceForm.split(" ") flatMap {
      token =>
        cluster.get(token) match {
          case Some(i) => "c%d,".format(i)
          case None => token + ","
        }
    })
  }

  override def requiresTokenization = true
  override def name: String = "Translation pair storage based on word clustering index."
}

