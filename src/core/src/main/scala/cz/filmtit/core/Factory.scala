package cz.filmtit.core

import concurrency.tokenizer.TokenizerWrapper
import concurrency.searcher.TranslationPairSearcherWrapper
import merge.LevenshteinMerger
import tm.{BackoffLevel, BackoffTranslationMemory}
import cz.filmtit.core.model.names.NERecognizer

import cz.filmtit.core.model.TranslationMemory
import opennlp.tools.namefind.{TokenNameFinderModel, NameFinderME}
import cz.filmtit.core.Utils.t2mapper

import java.sql.{SQLException, DriverManager, Connection}
import cz.filmtit.core.names.OpenNLPNameFinder
import search.postgres.impl.{PGFirstLetterStorage, FulltextStorage, NEStorage, FirstLetterStorage}
import cz.filmtit.share.annotations.AnnotationType
import rank._
import org.apache.commons.logging.LogFactory
import search.external.MosesServerSearcher
import scala.Some

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
    DriverManager.getConnection("jdbc:hsqldb:mem:filmtitdb;sql.syntax_pgs=true;check_props=true", "sa", "")
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
      configuration.maxNumberOfConcurrentSearchers,
      configuration.searcherTimeout,
      useInMemoryDB=useInMemoryDB
    )


  def createTM(
    l1: Language, l2: Language,
    connection: Connection,
    configuration: Configuration,
    maxNumberOfConcurrentSearchers: Int,
    searcherTimeout: Int,
    indexing: Boolean = false,
    useInMemoryDB: Boolean = false
  ): TranslationMemory = {

    var l1TokenizerWrapper: Option[TokenizerWrapper] = None
    var l2TokenizerWrapper: Option[TokenizerWrapper] = None

    var levels = List[BackoffLevel]()

    if (!useInMemoryDB) {
      //First level exact matching
      val flSearcher = new PGFirstLetterStorage(l1, l2, connection, useInMemoryDB)
      levels ::= new BackoffLevel(flSearcher, Some(new ExactWekaRanker(configuration.exactRankerModel)), 0.7, TranslationSource.INTERNAL_EXACT)

      //Second level: Full text search
      val fulltextSearcher = new FulltextStorage(l1, l2, connection)
      levels ::= new BackoffLevel(fulltextSearcher, Some(new FuzzyWekaRanker(configuration.fuzzyRankerModel)), 0.0, TranslationSource.INTERNAL_FUZZY)
    } else {
      val flSearcher = new FirstLetterStorage(l1, l2, connection, useInMemoryDB)
      levels ::= new BackoffLevel(flSearcher, Some(new ExactWekaRanker(configuration.exactRankerModel)), 0.7, TranslationSource.INTERNAL_EXACT)
    }

    if (!indexing) {
      //Third level: Moses
      val mosesSearchers = (1 to 30).map { _ =>
        new MosesServerSearcher(l1, l2, configuration.mosesURL)
      }.toList

      levels ::= new BackoffLevel(new TranslationPairSearcherWrapper(mosesSearchers, 30*60), None, 0.7, TranslationSource.EXTERNAL_MT)
    }

    if ( levels.map(_.searcher).exists(_.requiresTokenization) ) {
      l1TokenizerWrapper = Some(createTokenizerWrapper(l1, configuration))
      l2TokenizerWrapper = Some(createTokenizerWrapper(l2, configuration))
    }

    new BackoffTranslationMemory(l1, l2, levels.reverse, Some(new LevenshteinMerger()), l1TokenizerWrapper, l2TokenizerWrapper)
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
