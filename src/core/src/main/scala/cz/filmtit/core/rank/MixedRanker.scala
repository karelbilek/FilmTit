package cz.filmtit.core.rank

import cz.filmtit.core.model._
import cz.filmtit.core.model.Chunk._
import cz.filmtit.core.model.ScoredTranslationPair._
import com.swabunga.spell.engine.EditDistance
import org.apache.xmlbeans.impl.common.Levenshtein

/**
 * @author Joachim Daiber
 *
 *
 *
 */

class MixedRanker extends TranslationPairRanker {

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
    List[ScoredTranslationPair] = {
    pairs.map { pair =>
      val editDistance = Levenshtein.distance(chunk, pair.source)
      val genreMatches = if (mediaSource != null) {
        mediaSource.genres.size - pair.mediaSource.genres.intersect(mediaSource.genres).size
      } else {
        0
      }

      ScoredTranslationPair.fromTranslationPair(pair, (100 * editDistance) + (genreMatches))
    }.sorted
  }


  override def name = "Mixed methods for ranking"


}