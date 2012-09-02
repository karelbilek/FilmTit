
package cz.filmtit.core

import cz.filmtit.share.annotations.AnnotationType
import scala.xml._
import collection.mutable.HashMap
import cz.filmtit.share.Language
import java.io.{FileInputStream, InputStream, File}
import opennlp.tools.tokenize.{TokenizerModel, Tokenizer}

/**
 * Configuration for the external files and databases required by the TM.
 * This Configuration class reads an XML file that contains all properities.
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
  
  val mosesURL:java.net.URL = new java.net.URL("""http://"""+(XMLFile \ "mosesURL").text+"/RPC2")


  //Languages:
  val l1 = Language.fromCode((XMLFile \ "l1").text)  //en
  val l2 = Language.fromCode((XMLFile \ "l2").text)  //cs

  //Database:
  private val dbXML = XMLFile \ "database"
  val dbConnector: String = (dbXML \ "connector").text
  val dbUser: String =  (dbXML \ "user").text
  val dbPassword: String = (dbXML \ "password").text

  //Named entity recognition:
  val modelPath: String = (XMLFile \ "model_path").text
  val neRecognizers = HashMap[Language, List[Pair[AnnotationType, String]]]()

  
  //Read the NER models and them by their language
  (XMLFile \ "ner_models" \ "ner_model") foreach( ner_model => {
    val language_code = Language.fromCode( (ner_model \ "@language").text )
    val updated_models = neRecognizers.getOrElse(
      language_code, List[Pair[AnnotationType, String]]()
    ) ++ List(Pair(AnnotationType.fromDescription( (ner_model \ "@type").text ), modelPath + ner_model.text))
    neRecognizers.update(language_code, updated_models)
  })

  //Read Tokenizers from the configuration
  var tokenizers = HashMap[Language, TokenizerModel]()
  (XMLFile \ "tokenizers" \ "tokenizer") foreach( tokenizer => {
    val language = Language.fromCode( (tokenizer \ "@language").text )
    tokenizers(language) = new TokenizerModel(new FileInputStream(new File(modelPath, tokenizer.text)))
  })


  //Indexing:
  private val importXML = XMLFile \ "import"

  val dataFolder = new File((importXML \ "data_folder").text)
  val importBatchSize = (importXML \ "batch_size").text.toInt
  val importMediasourceCache = new File((importXML \ "mediasource_cache").text)
  val subtitlesFolder = new File((importXML \ "subtitles_folder").text)

  def getSubtitleName(s:String) = subtitlesFolder+"/"+s+".gz"
  def getDataFileName(s:String) = dataFolder+"/"+s+".txt"

  val fileMediasourceMapping = new File((importXML \ "file_mediasource_mapping").text)

  //Userspace:
  private val userspaceXML = XMLFile \ "userspace"
  val maximumSuggestionsCount = (userspaceXML \ "maximum_suggestions_count").text.toInt
  val sessionTimeout = (userspaceXML \ "session_timeout_limit").text.toLong
  val permanentSessionTimeout = (userspaceXML \ "permanent_session_timeout_limit").text.toLong
  val serverAddress = (userspaceXML \ "server_address").text
  //Userspace - mail  - configuration
  val userspaceXMLmail = userspaceXML \ "mail"
  val filmtitAddress =  (userspaceXMLmail \ "properties" \"filmtit_address").text
  val filmtitPassword =  (userspaceXMLmail \ "properties" \"filmtit_password").text


  //Core
  private val coreXML = XMLFile \ "core"
  var maxNumberOfConcurrentSearchers = (coreXML \ "max_number_of_concurrent_searchers").text.toInt
  val searcherTimeout:Int = (coreXML \ "searcher_timeout").text.toInt

  lazy val exactRankerWeights: List[Double] = (coreXML \ "ranking" \ "exact_ranker_weights").text.split(" ").map(_.toDouble).toList
  lazy val exactRankerModel = new File(modelPath, (coreXML \ "ranking" \ "exact_ranker_model").text)

    lazy val fuzzyRankerWeights: List[Double] = (coreXML \ "ranking" \ "fuzzy_ranker_weights").text.split(" ").map(_.toDouble).toList
    lazy val fuzzyRankerModel = new File(modelPath, (coreXML \ "ranking" \ "fuzzy_ranker_model").text)

  val configMail =new java.util.HashMap[String,String]()
  (userspaceXMLmail \ "properties" \ "entry") foreach( entry => {
    val property = (entry \ "@key").text
    configMail.put(property,entry.text)
  })

  val SeznamEndpoint = (userspaceXML \ "openid" \ "seznamcz").text
  val SeznamNick =   (userspaceXML \ "openid" \ "nickname").text


  val freebaseKey: String = (XMLFile \ "freebase_key").text



}

