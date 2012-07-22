package cz.filmtit.core.search.external

import java.net.{UnknownHostException, URLEncoder}
import io.Source
import org.apache.commons.logging.LogFactory
import org.json.{JSONArray, JSONObject}
import cz.filmtit.core.model.TranslationPairSearcher
import collection.mutable.ListBuffer
import cz.filmtit.share.{Language, TranslationPair, TranslationSource, Chunk}
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import opennlp.tools.tokenize.Tokenizer

/**
 * Translation pair searcher using the API of mymemory.translated.net
 *
 * The results contain both TM results and MT results.
 *
 * See [[http://mymemory.translated.net/doc/spec.php]]
 *
 * @author Joachim Daiber
 */

object MosesServerSearcher {
  val replaceSpacesRegex = """\s([.!?,])""".r
  val apoRegex = """^\p{Lu}""".r
  val capitalLetterRegex = """^\p{Lu}""".r
  val apostropheRegex = """'(\S)""".r
}

class MosesServerSearcher(
  l1: Language,
  l2: Language,
  l1tokenizer: Tokenizer,
  url: java.net.URL
) extends TranslationPairSearcher(l1, l2) {

  val logger = LogFactory.getLog(this.getClass.getSimpleName)
  
  val config = new XmlRpcClientConfigImpl();
  config.setServerURL(url);
  val client = new XmlRpcClient();
  client.setConfig(config);


  def sendToMoses(source:String):String = {
    
    println("MOJZIS - chci "+source);
// capital letter at the beginning /can cause trouble in named entities, but what are you gonna do/
     val (wasCapitalizedSource, uncapitalizedSource) = 
        if (MosesServerSearcher.capitalLetterRegex.findFirstIn(source).isDefined) {
            (true, source.toLowerCase)
        } else {
           (false, source)
        }


     val tokens:Seq[String]=try {
        l1tokenizer.tokenize(uncapitalizedSource)
     } catch {
        case e:Exception => println("DEMENCE "+source)
        throw e;
     }
     val retokenizedSource = tokens.reduceLeftOption(_+" "+_).getOrElse("")

	 val mosesParams = new java.util.HashMap[String,String]()
	 mosesParams.put("text", retokenizedSource)
	 mosesParams.put("align", "false")
	 mosesParams.put("report-all-factors", "false")
     val params = Array[Object](null)
	 params(0) = mosesParams


	 val result:java.util.HashMap[String, Object] = client.execute("translate", params) match {
        case m:java.util.HashMap[String, Object]=>m
        case _ => throw new ClassCastException("Wrong type of result from moses")
     }
	
     val translation = result.get("text") match {
        case s:String=> s
        case _ => throw new ClassCastException("Wrong type of result from moses")
     }
     val translationWithoutSpaces = MosesServerSearcher.replaceSpacesRegex.replaceAllIn(translation, "$1")
     
     val res = if (wasCapitalizedSource) {
        translationWithoutSpaces.capitalize
     } else {
        translationWithoutSpaces
     }
     val resReplacedApo = MosesServerSearcher.apostropheRegex.replaceAllIn(res, """' $1""");
     println("MOJZIS - DOSTAVAM "+resReplacedApo)
     resReplacedApo
 
  }
  
  def candidates(chunk: Chunk, language: Language): List[TranslationPair] = { 
    if (language != l1) {
      throw new Exception("Moses walked through the sea only one way.");
    }
    List[TranslationPair] (
       new TranslationPair(
          chunk,
          new Chunk(sendToMoses(chunk.getSurfaceForm)),
          TranslationSource.EXTERNAL_TM,
          1
        )
    )
  }
  
  

}
