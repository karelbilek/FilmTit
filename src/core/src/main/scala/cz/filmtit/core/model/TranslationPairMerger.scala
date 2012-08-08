package cz.filmtit.core.model

import cz.filmtit.share.TranslationPair

/**
 * @author Joachim Daiber
 */

abstract class TranslationPairMerger {

  def merge(pairs: List[TranslationPair], n: Int): List[TranslationPair]

}