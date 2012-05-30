package cz.filmtit.core.rank

import cz.filmtit.core.model.data._
import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._

/**
 * @author Joachim Daiber
 *
 *
 */
//TODO add more features
class ExactRanker extends BaseRanker {

  val lambdas = (0.95, 0.05)

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = {
    pair.setScore(
        lambdas._1 * 1.0 - 
        (
            (StringUtils.getLevenshteinDistance(
                chunk.getSurfaceForm, 
                pair.getChunkL1.getSurfaceForm) 
                / chunk.getSurfaceForm.length.toFloat
             )
        ) + (lambdas._2 * genreMatches(mediaSource, pair)))
    pair
  }

  override def name = "Exact Levensthein-based ranking."


}
