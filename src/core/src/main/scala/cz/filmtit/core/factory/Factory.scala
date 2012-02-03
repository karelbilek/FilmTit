package cz.filmtit.core.factory

import cz.filmtit.core.rank.ExactRanker
import cz.filmtit.core.tm.BackoffTranslationMemory
import cz.filmtit.core.model.names.{NERecognizer, NEType}
import cz.filmtit.core.model.{Language, TranslationMemory}

import opennlp.tools.namefind.{TokenNameFinderModel, NameFinderME}

import cz.filmtit.core.names.OpenNLPNameFinder
import java.io.{File, FileInputStream}
import opennlp.tools.tokenize.{WhitespaceTokenizer, Tokenizer}
import cz.filmtit.core.database.postgres.impl.{NEStorage, FirstLetterStorage}

/**
 * @author Joachim Daiber
 *
 */

object Factory {

  def createTM(): TranslationMemory = {

    //Second level fuzzy matching with NER:
    val fuzzyTM1 = new BackoffTranslationMemory(
      new NEStorage(Language.en, Language.cz),
      new ExactRanker()
    )

    //First level exact matching with backoff to fuzzy matching:
    new BackoffTranslationMemory(
      new FirstLetterStorage(Language.en, Language.cz),
      new ExactRanker(),
      backoff = Some(fuzzyTM1)
    )

  }


  def createNERecognizer(
    neType: NEType.NEType,
    language: Language.Language,
    modelFile: String
  ): NERecognizer = {

    val tokenNameFinderModel = new TokenNameFinderModel(
      new FileInputStream(modelFile)
    )

    new OpenNLPNameFinder(
      neType,
      new NameFinderME(tokenNameFinderModel),
      createTokenizer(language)
    )
  }

  def createTokenizer(language: Language.Language): Tokenizer = {
    WhitespaceTokenizer.INSTANCE
  }

}