package cz.filmtit.core.model

import cz.filmtit.share.TranslationPair

/**
 * Interface for merging translation pairs. Translation pairs should be merged if they are too similar.
 *
 * @author Joachim Daiber
 */

abstract class TranslationPairMerger {

  /**
   * Merge translation pairs such that pairs that are too similar are merged into one pair.
   *
   * This step should be performed after the translation pairs were ranked by a [[cz.filmtit.core.model.TranslationPairRanker]].
   *
   * @param pairs the scored translation pairs
   * @param n number of translation pair candidates that should be returned
   * @return the n best translation pairs, with similar pairs merged into a single pair
   */
  def merge(pairs: List[TranslationPair], n: Int): List[TranslationPair]

}