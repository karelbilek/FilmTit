/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

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
 * Factory for the default implementations of various classes.
 *
 * @author Joachim Daiber
 *
 */

object Factory {

  val logger = LogFactory.getLog("Factory")

  /**
   * Create a JDBC connection for an in-memory database.
   *
   * This is used for testing purposes.
   *
   * @return in-memory database JDBC connector
   */
  def createInMemoryConnection(): Connection = {
    Class.forName("org.hsqldb.jdbcDriver")
    DriverManager.getConnection("jdbc:hsqldb:mem:filmtitdb;sql.syntax_pgs=true;check_props=true", "sa", "")
  }


  /**
   * Create a production JDBC database connector.
   *
   * @param configuration the FilmTit configuration object
   * @param readOnly specifies if the database connection should be read-only
   * @return
   */
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

  /**
   * Create NE recognizers from the configuration using the two tokenizer wrappers.
   *
   * @param configuration the FilmTit configuration object
   * @param wrapperl1 the TokenizerWrapper for the first language
   * @param wrapperl2 the TokenizerWrapper for the second language
   * @return
   */
  def createNERecognizersFromConfiguration(configuration: Configuration, wrapperl1: TokenizerWrapper, wrapperl2: TokenizerWrapper) = {
    (createNERecognizers(configuration.l1, configuration, wrapperl1),
      createNERecognizers(configuration.l2, configuration, wrapperl2))

  }

  /**
   * Create a fully in-memory translation memory for testing.
   *
   * @param configuration the FilmTit configuration object
   * @return
   */
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

  /**
   * Create a production translation memory from the configuration.
   *
   * @param configuration the FilmTit configuration object
   * @param readOnly specifies if the database connection should be read-only
   * @param useInMemoryDB
   * @return
   */
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


  /**
   * Create a translation memory from exact parameters.
   *
   * @param l1 the first language
   * @param l2 the second language
   * @param connection the database connection
   * @param configuration the FilmTit configuration object
   * @param maxNumberOfConcurrentSearchers the maximum number of searcher running concurrently
   * @param searcherTimeout maximum timeout for searchers
   * @param indexing specifies whether the TM is created for indexing purposes
   * @param useInMemoryDB specifies whether an in-memory database should be used
   * @return
   */
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
      levels ::= new BackoffLevel(fulltextSearcher, Some(new FuzzyWekaRanker(configuration.fuzzyRankerModel)), 0.4, TranslationSource.INTERNAL_FUZZY)
    } else {
      val flSearcher = new FirstLetterStorage(l1, l2, connection, useInMemoryDB)
      levels ::= new BackoffLevel(flSearcher, Some(new ExactWekaRanker(configuration.exactRankerModel)), 0.7, TranslationSource.INTERNAL_EXACT)
    }

    if (!indexing) {
       //Third level: Moses 
      val regexesBefore = Seq(
         ("""'(\S)""".r, """&apos;$1"""), //replacing apostrophe
         ("""(\S)&apos;""".r, """$1 &apos;"""), //space before apostrophe
         ("""\s+n &apos;t""".r , "n &apos;t")  //n't
      )
    
      val regexesAfter = Seq(
        ("""\s([.!?,])""".r, "$1"), //space before diacritics
        (""" &apos; """.r, """'""")  //apostrophe back
      )
      
      
      val mosesSearchers = (1 to 30).map { _ =>
        new MosesServerSearcher(l1, l2,regexesBefore, regexesAfter, configuration.mosesURL)
      }.toList

      levels ::= new BackoffLevel(new TranslationPairSearcherWrapper(mosesSearchers, 30*60), None, 0.5, TranslationSource.EXTERNAL_MT)
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

  /**
   * Create a tokenizer wrapper for the specified language from the configuration.
   *
   * @param language the language of the tokenizer wrapper
   * @param conf the FilmTit configuration object
   * @return
   */
  def createTokenizerWrapper(language:Language, conf:Configuration) = {
    val tokenizers = (0 to 10).par.map{_=>createTokenizer_(language, conf)}
    new TokenizerWrapper(tokenizers, conf.searcherTimeout)

  }
}
