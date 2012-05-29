package cz.filmtit.core

import cz.filmtit.core.model.annotation.ChunkAnnotation
import scala.xml._
import collection.mutable.HashMap
import cz.filmtit.share.Language
import java.io.{FileInputStream, InputStream, File}

/**
 * Configuration file for the external files and databases required by the TM.
 *
 * @author Joachim Daiber
 */

class Configuration(configurationFile: InputStream) {
  
  def this(configurationFile: File) {
    this(new FileInputStream(configurationFile))
  }

  def this(fileAddress: String) {
    this(new File(fileAddress))
  }

  private val XMLFile = XML.load(configurationFile)

  //Languages:
  val l1 = Language.fromCode((XMLFile \ "l1").text)
  val l2 = Language.fromCode((XMLFile \ "l1").text)

  //Database:
  private val dbXML = XMLFile \ "database"
  val dbConnector: String = (dbXML \ "connector").text
  val dbUser: String =  (dbXML \ "user").text
  val dbPassword: String = (dbXML \ "password").text

  //Named entity recognition:
  val modelPath: String = (XMLFile \ "model_path").text
  val neRecognizers = HashMap[Language, List[Pair[ChunkAnnotation, String]]]()

  //Read the NER models and them by their language
  (XMLFile \ "ner_models" \ "ner_model") foreach( ner_model => {
    val language_code = Language.fromCode( (ner_model \ "@language").text )
    val updated_models = neRecognizers.getOrElse(
      language_code, List[Pair[ChunkAnnotation, String]]()
    ) ++ List(Pair(ChunkAnnotation.fromName( (ner_model \ "@type").text ), modelPath + ner_model.text))
    neRecognizers.update(language_code, updated_models)
  })

  //Indexing:
  private val importXML = XMLFile \ "import"

  val dataFolder = new File((importXML \ "data_folder").text)
  val importBatchSize = (importXML \ "batch_size").text.toInt
  val importIMDBCache = new File((importXML \ "imdb_cache").text)

  val fileMediasourceMapping = new File((importXML \ "file_mediasource_mapping").text)

  val expectedNumberOfTranslationPairs = (importXML \ "expected_number_of_translationpairs").text.toInt

  private val heldoutXML = importXML \ "heldout"
  val heldoutSize = (heldoutXML \ "size").text.toDouble //percentage of all data
  val heldoutFile = new File((heldoutXML \ "path").text)

  //Userspace:
  private val userspaceXML = XMLFile \ "userspace";
  val sessionTimeout = (userspaceXML \ "session_timeout_limit").text.toLong


  //Core
  private val coreXML = XMLFile \ "core";
  var maxNumberOfConcurrentSearchers = (coreXML \ "max_number_of_concurrent_searchers").text.toInt
  val searcherTimeout:Int = (coreXML \ "searcher_timeout").text.toInt
}
