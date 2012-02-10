package cz.filmtit.core

import cz.filmtit.core.rank.ExactRanker
import cz.filmtit.core.tm.BackoffTranslationMemory
import cz.filmtit.core.model.names.NERecognizer

import opennlp.tools.namefind.{TokenNameFinderModel, NameFinderME}

import cz.filmtit.core.names.OpenNLPNameFinder
import java.io.FileInputStream
import opennlp.tools.tokenize.{WhitespaceTokenizer, Tokenizer}
import cz.filmtit.core.search.postgres.impl.{NEStorage, FirstLetterStorage}
import cz.filmtit.core.model.annotation.ChunkAnnotation
import cz.filmtit.core.model.{Language, TranslationMemory}
import search.google.GoogleTranslateSearcher

/**
 * @author Joachim Daiber
 *
 */

object Factory {

  def createTM(): TranslationMemory = {

    //Third level: Google translate
    val googleTM = new BackoffTranslationMemory(
      new GoogleTranslateSearcher(Language.en, Language.cz),
      new ExactRanker()
    )

    //Second level fuzzy matching with NER:
    val neTM = new BackoffTranslationMemory(
      new NEStorage(Language.en, Language.cz),
      new ExactRanker(),
      threshold = 0.0
    )

    //First level exact matching with backoff to fuzzy matching:
    new BackoffTranslationMemory(
      new FirstLetterStorage(Language.en, Language.cz),
      new ExactRanker(),
      backoff = Some(neTM)
    )

  }


  def createNERecognizer(
    neType: ChunkAnnotation,
    language: Language,
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

  def createNERecognizers(l: Language): List[NERecognizer] = {
    Configuration.neRecognizers.get(l) match {
      case Some(recognizers) => recognizers map {
        pair => {
          val (neType, modelFile) = pair
          Factory.createNERecognizer(neType, l, modelFile)
        }
      }
      case None => List()
    }
  }

  def createNERecognizer(l: Language, t: ChunkAnnotation): NERecognizer =
    createNERecognizers(l).filter( _.neClass == t ).head

  def createTokenizer(language: Language): Tokenizer = {
    WhitespaceTokenizer.INSTANCE
  }
}