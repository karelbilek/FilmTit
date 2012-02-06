package cz.filmtit.core.factory

import cz.filmtit.core.rank.ExactRanker
import cz.filmtit.core.tm.BackoffTranslationMemory
import cz.filmtit.core.model.names.{NERecognizer}

import opennlp.tools.namefind.{TokenNameFinderModel, NameFinderME}

import cz.filmtit.core.names.OpenNLPNameFinder
import java.io.FileInputStream
import opennlp.tools.tokenize.{WhitespaceTokenizer, Tokenizer}
import cz.filmtit.core.database.postgres.impl.{NEStorage, FirstLetterStorage}
import cz.filmtit.core.model.annotation.ChunkAnnotation
import cz.filmtit.core.model.{Language, TranslationMemory}

/**
 * @author Joachim Daiber
 *
 */

object Factory {

  def createTM(): TranslationMemory = {

    //Second level fuzzy matching with NER:
    val fuzzyTM1 = new BackoffTranslationMemory(
      new NEStorage(Language.en, Language.cz),
      new ExactRanker(),
      threshold = 0.0
    )

    //First level exact matching with backoff to fuzzy matching:
    new BackoffTranslationMemory(
      new FirstLetterStorage(Language.en, Language.cz),
      new ExactRanker(),
      backoff = Some(fuzzyTM1)
    )

  }


  def createNERecognizer(
    neType: ChunkAnnotation,
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