package cz.filmtit.core.rank

import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._
import cz.filmtit.core.model.Patterns
import scala.Double

/**
 * @author Joachim Daiber
 */

class FuzzyRanker(weights: List[Double]) extends LinearInterpolationRanker(weights) {

  def getScoreNames = List("prior_score", "count", "edit_distance", "genres", "match_length_penaltiy", "translation_length_penaltiy", "punctuation")

  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double] = {

    List(

      pair.getScore,

      //TranslationPair support
      (pair.getCount / totalCount.toDouble),

      //Levenshtein:
      1.0 - math.min(StringUtils.getLevenshteinDistance(chunk.getSurfaceForm, pair.getChunkL1.getSurfaceForm) / chunk.getSurfaceForm.length.toFloat, 1.0),

      //Genre matches:
      genreMatches(mediaSource, pair),

      //Length difference between query and source
      if (pair.getStringL1.length < chunk.getSurfaceForm.length)
        pair.getStringL1.length / chunk.getSurfaceForm.length.toDouble
      else
        chunk.getSurfaceForm.length / chunk.getSurfaceForm.length.toDouble
      ,

      //Length difference between source and translation
      if (pair.getStringL1.length < pair.getStringL2.length)
        pair.getStringL1.length / pair.getStringL2.length.toDouble
      else
        pair.getStringL2.length / pair.getStringL1.length.toDouble
      ,

      //Does final punctuation match?
      pair.getStringL2.last match {
        case Patterns.punctuation() if pair.getStringL2.last != chunk.getSurfaceForm.last => 0.0
        case _ => 1.0
      }
    )
  }

  override def name = "Ranking for fuzzy matches."

}
