package cz.filmtit.core.model

import data.{Chunk, MediaSource, ScoredTranslationPair, TranslationPair}


/**
 * Assigns scores to translation pairs retrieved from the database or external
 * services.
 *
 * @author Joachim Daiber
 */

abstract class TranslationPairRanker {


  /**
   * Rank the list of translation pairs according to a score of how well
   * they match the Chunk and source MediaSource. The best match will
   * be the first in the list.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pairs the translation pair candidates
   * @return sorted list of scored translation pairs with best first
   */
  def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
  List[ScoredTranslationPair] =
    pairs.map(pair => rankOne(chunk, mediaSource, pair)).sorted


  /**
   * Return only the best translation pair for the Chunk and MediaSource.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pairs the translation pair candidates
   * @return best-match translation pair with score
   */
  def best(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]):
  Option[ScoredTranslationPair] =
    pairs.map(pair => rankOne(chunk, mediaSource, pair)) match {
      case List()                                  => None
      case x: List[Ordered[ScoredTranslationPair]] => Some(x.max.asInstanceOf[ScoredTranslationPair])
    }



  /**
   * Scores a single translation pair according to match with Chunk and
   * MediaSource.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pair the translation pair candidate
   * @return translation pair with score
   */
  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair):
  ScoredTranslationPair


  /**
   * Every ranker should have a name.
   */
  def name: String

}
