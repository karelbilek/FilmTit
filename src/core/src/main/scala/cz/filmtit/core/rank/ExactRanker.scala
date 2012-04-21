package cz.filmtit.core.rank

import cz.filmtit.core.model.data._
import org.apache.commons.lang3.StringUtils
import cz.filmtit.share.{TranslationPair, MediaSource}

/**
 * @author Joachim Daiber
 *
 *
 */
//TODO add more features
class ExactRanker extends BaseRanker {

  val lambdas = (0.95, 0.05)

  def rankOne(chunk: AnnotatedChunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = {
    pair.setScore(lambdas._1 * 1.0 - ((StringUtils.getLevenshteinDistance(chunk.getSurfaceform, pair.getChunkL1.getSurfaceform) / chunk.surfaceform.length.toFloat)) + (lambdas._2 * genreMatches(mediaSource, pair)))
    pair
  }

  override def name = "Exact Levensthein-based ranking."


}