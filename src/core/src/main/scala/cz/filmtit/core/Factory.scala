package cz.filmtit.core

import concurrency.tokenizer.TokenizerWrapper
import concurrency.searcher.TranslationPairSearcherWrapper
import cz.filmtit.core.tm.BackoffTranslationMemory
import cz.filmtit.core.model.names.NERecognizer

import cz.filmtit.core.model.TranslationMemory
import opennlp.tools.namefind.{TokenNameFinderModel, NameFinderME}
import cz.filmtit.core.Utils.t2mapper

import java.sql.{SQLException, DriverManager, Connection}
import cz.filmtit.core.names.OpenNLPNameFinder
import cz.filmtit.core.search.postgres.impl.{NEStorage, FirstLetterStorage}
import cz.filmtit.share.annotations.AnnotationType
import cz.filmtit.core.rank.{FuzzyNERanker, ExactRanker}
import org.apache.commons.logging.LogFactory
import search.external.MosesServerSearcher
//import search.external.MyMemorySearcher
import cz.filmtit.share.{Language, TranslationSource}
import cz.filmtit.core.Factory._
import collection.mutable.HashMap
import java.io.{File, FileInputStream}
import opennlp.tools.tokenize.{TokenizerME, WhitespaceTokenizer, Tokenizer}

/**
 * Factories for default implementations of various classes
 *
 * @author Joachim Daiber
 *
 */

object Factory {

  val logger = LogFactory.getLog("Factory")

  def createInMemoryConnection(): Connection = {
    Class.forName("org.hsqldb.jdbcDriver")
    DriverManager.getConnection("jdbc:hsqldb:mem:filmtitdb", "sa", "")
  }

  def createConnection(configuration: Configuration, readOnly: Boolean = true): Connection = {
    Class.forName("org.postgresql.Driver")

    val connection:Connection = try {
      DriverManager.getConnection(
        configuration.dbConnector,
        configuration.dbUser,
        configuration.dbPassword)
    } catch {
      case e: SQLException => {
        System.err.println("I could not connect to database %s. Please check if the DBMS is running and database exists.".format(configuration.dbConnector))
        println(e);
        System.exit(1)
        null
      }
    }

    //Assure the database is in read-only mode if required.
    if (readOnly == true)
      connection.setReadOnly(true)

    connection
  }

  def createNERecognizersFromConfiguration(configuration: Configuration, wrapperl1:TokenizerWrapper, wrapperl2:TokenizerWrapper) = {
    (createNERecognizers(configuration.l1, configuration, wrapperl1), 
    createNERecognizers(configuration.l2, configuration, wrapperl2))
    
  }

  def createInMemoryTM(configuration: Configuration): TranslationMemory = {
    val connection = createInMemoryConnection()

    createTM(
      configuration.l1,
      configuration.l2,
      connection,
      configuration,
      useInMemoryDB = true,
      maxNumberOfConcurrentSearchers = configuration.maxNumberOfConcurrentSearchers,
      searcherTimeout = configuration.searcherTimeout)
  }

  def createTMFromConfiguration(
    configuration: Configuration,
    readOnly: Boolean = true,
    useInMemoryDB: Boolean = false) : TranslationMemory = 
   
    createTM(
      configuration.l1, configuration.l2,
      if (useInMemoryDB) createInMemoryConnection() else createConnection(configuration, readOnly),
      configuration,
      useInMemoryDB,
      configuration.maxNumberOfConcurrentSearchers,
      configuration.searcherTimeout
  )
  

  def createTM(
    l1: Language, l2: Language,
    connection: Connection,
    configuration: Configuration,
    useInMemoryDB: Boolean = false,
    maxNumberOfConcurrentSearchers: Int,
    searcherTimeout: Int
    ): TranslationMemory = {

    val csTokenizerWrapper = createTokenizerWrapper(Language.CS, configuration)
    val enTokenizerWrapper = createTokenizerWrapper(Language.EN, configuration)

    //Third level: Moses
    val mtTM = new BackoffTranslationMemory(
      new MosesServerSearcher(
        Language.EN,
        Language.CS,
 //       enTokenizer,
        configuration.mosesURL,
        30, 60*20
      ),
      Language.EN,
      Language.CS,
      threshold = 0.7
    )

    val neSearchers = (1 to maxNumberOfConcurrentSearchers).map { _ =>
      val recognizers = createNERecognizersFromConfiguration(configuration, csTokenizerWrapper, enTokenizerWrapper)
      new NEStorage(Language.EN, Language.CS, connection, recognizers._1, recognizers._2, useInMemoryDB)
    }.toList

    //Second level fuzzy matching with NER:
    val neTM = new BackoffTranslationMemory(
      new TranslationPairSearcherWrapper(neSearchers, searcherTimeout),
      Language.EN,
      Language.CS,
       Some(new FuzzyNERanker()),
      threshold = 0.2,
      backoff = Some(mtTM)
    )

    //First level exact matching with backoff to fuzzy matching:
    new BackoffTranslationMemory(
      new FirstLetterStorage(Language.EN, Language.CS, connection, enTokenizerWrapper, csTokenizerWrapper, useInMemoryDB),
      l1=Language.EN,
      l2=Language.CS,
      ranker= Some(new ExactRanker()),
      threshold = 0.8,
      backoff = Some(neTM),
      tokenizerl1= Some(enTokenizerWrapper),
      tokenizerl2 = Some(csTokenizerWrapper)
    )
  }


  val neModels = HashMap[String, TokenNameFinderModel]()

  /**
   * Build all NE recognizers specified for the language in
   * [[org.scalatest.prop.Configuration]].
   *
   * @param language the language the NE recognizers work on
   * @return
   */
  def createNERecognizers(language: Language, configuration: Configuration, tokenizer:TokenizerWrapper): List[NERecognizer] = {
    configuration.neRecognizers.get(language) match {
      case Some(recognizers) => recognizers map {
        pair => {
          val (neType, modelFile) = pair

          val model: TokenNameFinderModel = neModels.getOrElseUpdate(
            modelFile,
            new TokenNameFinderModel(new FileInputStream(modelFile))
          )

          logger.debug("Creating NE recognizer (%s, %s)".format(neType, language))
          new OpenNLPNameFinder(
            neType,
            new NameFinderME(model),
            tokenizer
          )
        }
      }
      case None => List()
    }
  }


  /**
   * Build a NE recognizer from [[cz.filmtit.core.Configuration]].
   *
   * @param language the language the NE recognizers works on
   * @param neType the type of NE, the recognizer detects
   * @return
   */
  def createNERecognizer(language: Language, neType: AnnotationType, configuration: Configuration, wrapper:TokenizerWrapper): NERecognizer =
    createNERecognizers(language, configuration, wrapper).filter( _.neClass == neType ).head


  /**
   * Build the default Tokenizer for a language.
   *
   * @param language language to be tokenized
   * @return
   */
  def createTokenizer_(language: Language): Tokenizer = {
    WhitespaceTokenizer.INSTANCE
  }

  /**
   * Build a Tokenizer for the language with a model
   * specified in the Configuration.
   *
   * @param language language to be tokenized
   * @return
   */
  def createTokenizer_(language: Language, configuration: Configuration): Tokenizer = {
    if (configuration.tokenizers contains language) {
      logger.debug("Creating ME tokenizer (%s)".format(language))
      new TokenizerME(configuration.tokenizers(language))
    } else {
      logger.debug("Creating default tokenizer (%s)".format(language))
      createTokenizer_(language)
    }
  }

  def createTokenizerWrapper(language:Language, conf:Configuration) = {
      val tokenizers = (0 to 10).par.map{_=>createTokenizer_(language, conf)}
      new TokenizerWrapper(tokenizers, conf.searcherTimeout)

  }
}
