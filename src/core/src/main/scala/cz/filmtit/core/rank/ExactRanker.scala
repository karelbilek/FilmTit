package cz.filmtit.core.rank

import cz.filmtit.core.model.data._
import org.apache.commons.lang3.StringUtils

/**
 * @author Joachim Daiber
 *
 * TODO add more features
 */

class ExactRanker extends BaseRanker {

  val lambdas = (0.95, 0.05)

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair):
  ScoredTranslationPair =
    ScoredTranslationPair.fromTranslationPair(pair,
      (lambdas._1 * 1.0 - ((StringUtils.getLevenshteinDistance(chunk, pair.chunkL1) / chunk.length.toFloat)))
        + (lambdas._2 * genreMatches(mediaSource, pair))
    )

  override def name = "Exact Levensthein-based ranking."


}