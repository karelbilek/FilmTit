package cz.fimtit.eval.database

import cz.filmtit.core.model._
import scala.collection.mutable.HashMap
import scala.io.Source


/**
 * @author Joachim Daiber
 *
 */

class PostgresClusteredStorage extends PostgresSignatureBasedStorage {

  def indexName = "idx_clustered"

  val cluster: HashMap[String, Int] = new HashMap[String, Int]()

  /*
   * Read in the result of the clustering, where each line looks like this:
   * {token}\t{cluster number}
   *
   * This clustering can be produced using mkcls (http://www-i6.informatik.rwth-aachen.de/web/Software/mkcls.html):
   * $ ./mkcls -c1500 -n10 -pcorpus.1m.txt -Vout
   */
  Source.fromFile("/Users/jodaiber/Desktop/out").getLines().foreach({ line: String  => {
    val Array(token, clusterID) = line.trim().split("\t")
    cluster.put(token, clusterID.toInt)
  }
  })

  override def signature(chunk: Chunk): String = {
    new String(chunk.surfaceform.split(" ") flatMap {token =>
      cluster.get(token) match {
        case Some(i) => "c%d,".format(i)
        case None => token + ","
      }
    })
  }

  override def name: String = "Translation pair storage based on word clustering index."
}

