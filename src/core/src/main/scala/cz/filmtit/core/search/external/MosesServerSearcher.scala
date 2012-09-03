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

package cz.filmtit.core.search.external

import cz.filmtit.core.model.TranslationPairSearcher
import cz.filmtit.share.{Language, TranslationPair, TranslationSource, Chunk}
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import cz.filmtit.share.exceptions.{LanguageNotSupportedException, SearcherNotAvailableException}
import scala.util.matching.Regex

/**
 * Translation pair searcher using standard Moses server 
 *
 * The server doesn't require any identification.
 *
 * Both the input and the output have to be very slightly
 * adjusted.
 *
 * The server translate only in one way and returns just 
 * one translation result.
 *
 * @author Karel Bílek
 */

/**
 * Helper object to hold all different regexes, that are needed
 * to make the Moses translation look either "nicer" (+-detokenizing),
 * or the other way - better for
 * Moses (+- tokenizing). We also have to sort that out
 */
object MosesServerSearcher {


  /**
   * Checks if sentence begins with capital letter, so we can lowercase it before sending it to
   * Moses (and then, re-upper case it)
   */
  val capitalLetterRegex = """^\p{Lu}""".r

  /**
   *  Detects multiple spaces.
   */
  val spaceRegex = """\s+(\s)""".r

  /**
   * Detects unknown words.
   */
  val unkRegex = """\|UNK""".r


  /**
   * Runs a list of regexes on string.
   * @param string String to run the regexes on
   * @param regexes Sequence of regexes together with what to replace it on. It is run
   *                from left to right.
   * @return Resulting string.
   */
  def cleanUpString(string:String, regexes:Seq[Pair[Regex, String]]):String =
    regexes.foldLeft(string){
      case (string, (regex, replacement)) => regex.replaceAllIn(string, replacement)
    }
}


/**
 * Moses searcher, that sends the sentence tokenized to remote moses server.
 * @constructor Creates a MosesServerSearcher.
 * @param l1 Source language
 * @param l2 Target language
 * @param regexesBeforeServer regexes that are run before sending to moses
 * @param regexesAfterServer regexes, that are run after moses (don't )
 * @param url Url of the remote Moses server (url:port)
 * @param genericTranslationScore The score with which the moses searcher returns the sentences
 * @param numberOfTries How many times is the Moses server called
 * @param repeatAfterMilliseconds After how many milliseconds is server contacted again?
 */
class MosesServerSearcher(
  l1: Language,
  l2: Language,

  regexesBeforeServer: Seq[Pair[Regex, String]],
  regexesAfterServer: Seq[Pair[Regex, String]],

  url: java.net.URL,
  genericTranslationScore: Double = 0.7,
  numberOfTries:Int = 4,
  repeatAfterMilliseconds:Long = 1000
) extends TranslationPairSearcher(l1, l2) {

  val config = new XmlRpcClientConfigImpl()
  config.setServerURL(url)
  val client = new XmlRpcClient()
  client.setConfig(config)

  /**
   * Only sends the text to server. Tries it 4 times with the pause of 1 second.
   * @param source The <b>exact</b> text to send to server.
   * @return What <b>exactly</b> server returned. Without any cleanup.
   */
  def getRawTranslation(source:String):String = {
    val mosesParams = new java.util.HashMap[String,String]()
    mosesParams.put("text", source)
    mosesParams.put("align", "false")
    mosesParams.put("report-all-factors", "false")
    val params = Array[Object](null)
    params(0) = mosesParams

    val tries = Iterator.continually{
        try {
            val result:java.util.HashMap[String, Object] = client.execute("translate", params) match {
            case m:java.util.HashMap[String, Object]=>m
            case _ => throw new ClassCastException("Wrong type of result from moses")
            }

            val translation = result.get("text") match {
                case s:String=> s
                case _ => throw new ClassCastException("Wrong type of result from moses")
            }
            (Some(translation), None)
        } catch {
            case e:Exception=>
            Thread.sleep(repeatAfterMilliseconds)
            (None, Some(e))
        }
   }.take(numberOfTries).toIterable
   
   val res = tries.find{_._1.isDefined}
   if (res.isDefined) {
     res.get._1.get
   } else {
     throw new SearcherNotAvailableException("Could not connect to Moses server.", tries.last._2.get)
   }


  }

  /**
   * Sends the string to moses and does some cleanup.
   * @param sourceTokens Already tokenized source string
   * @return Cleaned up result.
   */
  def prepareAndSendToMoses(sourceTokens:Array[String]):String = {
   
    import MosesServerSearcher._

    if (sourceTokens.size ==0) {
        return ""
    }

    // capital letter at the beginning /can cause trouble in named entities, but what are you gonna do/
     val (wasCapitalizedSource, uncapitalizedTokens) = 
        if (MosesServerSearcher.capitalLetterRegex.findFirstIn(sourceTokens(0)).isDefined) {
            (true, sourceTokens(0).toLowerCase +: sourceTokens.tail)
        } else {
           (false, sourceTokens)
        }

     val joinedSource = uncapitalizedTokens.reduceLeftOption(_+" "+_).get



     val cleanedUpSource = cleanUpString(joinedSource, regexesBeforeServer)


     val translation = getRawTranslation(cleanedUpSource)

     val cleanedUpTarget = cleanUpString(translation,
       Pair(spaceRegex, " ")+:Pair(unkRegex, "")+:regexesAfterServer)

     
     val res = if (wasCapitalizedSource) {
       cleanedUpTarget.capitalize
     } else {
       cleanedUpTarget
     }


     res
 
  }

  /**
   * Queries moses for a chunk
   * @param chunk Chunk to translate
   * @param language Language of the chunk (must be l1 of the searcher)
   * @return The one candidate from Moses (it never returns more than one)
   */
  def candidates(chunk: Chunk, language: Language): List[TranslationPair] = { 
    if (language != l1) {
      throw new LanguageNotSupportedException("Moses can translate from "+l1.getName+" to "+l2.getName+", you requested "+language.getName)
    }
    List[TranslationPair] (
       new TranslationPair(
          chunk,
          new Chunk(prepareAndSendToMoses(chunk.getTokens)),
          TranslationSource.MOSES,
          genericTranslationScore
        )
    )
  }

  /**
   * Does nothing.
   */
  def close() {}

  /**
   * Moses requires tokenization, even when it's then slightly changed by regexes.
   * @return True.
   */
  def requiresTokenization = true

  /**
   * Name of searcher.
   * @return "Moses" string.
   */
  override def toString = "Moses"
}
