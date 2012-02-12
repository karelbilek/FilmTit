package cz.filmtit.core.search.google

import cz.filmtit.core.model.{TranslationPairSearcher, Language}
import cz.filmtit.core.model.data.{TranslationPair, Chunk}


/**
 * @author Joachim Daiber
 *
 *
 *
 */

class GoogleTranslateSearcher(l1: Language, l2: Language)
extends TranslationPairSearcher(l1, l2) {

  def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {
    //TODO implement
    List[TranslationPair]()
  }


}
