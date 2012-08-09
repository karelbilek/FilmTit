package cz.filmtit.core.rank

import _root_.java.io.File
import org.apache.commons.lang3.StringUtils
import cz.filmtit.share._
import cz.filmtit.core.Utils.min
import cz.filmtit.core.model.Patterns
import scala.Double

/**
 * @author Joachim Daiber
 */

trait ExactRanker extends BaseRanker {

  def getScoreNames = List("count", "edit_distance", "genres", "translation_length_penaltiy", "punctuation")

  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double] = {

    List(
      //TranslationPair support
      (pair.getCount / totalCount.toDouble),

      //Levenshtein:
      1.0 - math.min(StringUtils.getLevenshteinDistance(chunk.getSurfaceForm, pair.getChunkL1.getSurfaceForm) / chunk.getSurfaceForm.length.toFloat, 1.0),

      //Genre matches:
      genreMatches(mediaSource, pair),

      //Length difference between source and translation
      if (pair.getStringL1.length < pair.getStringL2.length)
        pair.getStringL1.length / pair.getStringL2.length.toDouble
      else
        pair.getStringL2.length / pair.getStringL1.length.toDouble
      ,

      //Does final punctuation match?
      punctuationMatches(chunk.getSurfaceForm, pair.getStringL2)
    )
  }

  override def name = "Exact Levensthein-based ranking."

}

class ExactWekaRanker(modelFile: File) extends WEKARanker(modelFile) with ExactRanker
class ExactLRRanker(weights: List[Double]) extends LinearInterpolationRanker(weights: List[Double]) with ExactRanker