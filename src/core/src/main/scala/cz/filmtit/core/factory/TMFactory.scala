package cz.filmtit.core.factory

import cz.filmtit.core.database.PostgresFirstLetterStorage
import cz.filmtit.core.model.{Language, TranslationMemory}
import cz.filmtit.core.rank.ExactRanker
import cz.filmtit.core.tm.BackoffTranslationMemory

/**
 * @author Joachim Daiber
 *
 */

object TMFactory {

  def defaultTM(): TranslationMemory = {

    //val fuzzyTM = BackoffTranslationMemory(
    //  ...,
    //  ...
    //)

    new BackoffTranslationMemory(
      new PostgresFirstLetterStorage(Language.en, Language.cz),
      new ExactRanker(),
      backoff=None
    )
  }

}