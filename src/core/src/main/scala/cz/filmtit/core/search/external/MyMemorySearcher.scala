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

import java.net.{UnknownHostException, URLEncoder}
import io.Source
import org.apache.commons.logging.LogFactory
import org.json.{JSONArray, JSONObject}
import cz.filmtit.core.model.TranslationPairSearcher
import collection.mutable.ListBuffer
import cz.filmtit.share.{Language, TranslationPair, TranslationSource, Chunk}

/**
 * Translation pair searcher using the API of mymemory.translated.net
 *
 * The results contain both TM results and MT results.
 *
 * See [[http://mymemory.translated.net/doc/spec.php]]
 *
 * @author Joachim Daiber
 */

class MyMemorySearcher(
  l1: Language,
  l2: Language,
  allowedSources: Set[TranslationSource] = Set(
    TranslationSource.EXTERNAL_MT,
    TranslationSource.EXTERNAL_TM
  )
) extends TranslationPairSearcher(l1, l2) {

  val logger = LogFactory.getLog(this.getClass.getSimpleName)

  val limit = 5
  val apiURL = (l1: String, l2: String, text: String) =>
    "http://mymemory.translated.net/api/get?langpair=%s|%s&q=%s"
      .format(l1, l2, URLEncoder.encode(text, "utf-8"))

  def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {

    val apiResponse = try {
      new JSONObject(
        Source.fromURL(
          apiURL(language.getCode, {if (language == l1) l2.getCode else l1.getCode}, chunk.getSurfaceForm)
        ).getLines().next()
      )
    } catch {
      case e: UnknownHostException => {
        logger.warn("Could not reach MyTranslate server.")
        return List[TranslationPair]()
      }
    }

    //TODO: add exception handling!
    if (apiResponse.getInt("responseStatus") equals 403)
      logger.info("Daily quota exceeded.")

    val candidates = ListBuffer[TranslationPair]()
    val matches: JSONArray = try {
        apiResponse.getJSONArray("matches")
    } catch {
        case e:org.json.JSONException => new JSONArray()
    }

    //Retrieve all matches:
    for (i <- 0 to math.min(matches.length() - 1, limit)) {
      val translation = matches.getJSONObject(i)

      //Set the chunks for the resulting translation pair
      val chunkL1 = if (language == l1) chunk else new Chunk(translation.getString("translation"))
      val chunkL2 = if (language == l1) new Chunk(translation.getString("translation")) else chunk

      //Check the source of the translation:
      val source = translation.getString("created-by") match {
        case "MT!" => TranslationSource.EXTERNAL_MT
        case _ => TranslationSource.EXTERNAL_TM
      }

      //Use the quality metric as the prior score of the TP:
      //TODO: second score: "match"?

      val quality = translation.getString("quality") match {
        case "" => 0.0
        case s: String => s.toInt * 0.01
      }

      if(allowedSources contains source)
        candidates += new TranslationPair(
          chunkL1,
          chunkL2,
          source,
          quality
        )
    }

    candidates.toList
  }

  def close() {}

  def requiresTokenization = false
}
