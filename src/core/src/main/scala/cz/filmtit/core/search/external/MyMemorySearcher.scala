package cz.filmtit.core.search.external

import io.Source
import java.net.URLEncoder
import org.json.{JSONArray, JSONObject}
import cz.filmtit.core.model.{TranslationSource, Language, TranslationPairSearcher}
import collection.mutable.ListBuffer
import cz.filmtit.core.model.data.{ScoredTranslationPair, TranslationPair, Chunk}

/**
 *
 * See [[http://mymemory.translated.net/doc/spec.php]]
 *
 * @author Joachim Daiber
 */

class MyMemorySearcher(l1: Language, l2: Language) extends TranslationPairSearcher(l1, l2) {

  val limit = 5
  val baseURL = "http://mymemory.translated.net/api/get?langpair=%s|%s&q=%s"

  def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {

    val v = new JSONObject(
      Source.fromURL(
        baseURL.format(
          language.code,
          if (language == l1) l2.code else l1.code,
          URLEncoder.encode(chunk.surfaceform, "utf-8")
        )
      ).getLines().next())

    //TODO: add exception handling!

    val candidates = ListBuffer[TranslationPair]()
    val matches: JSONArray = v.getJSONArray("matches")

    //Retrieve all matches:
    for (i <- 0 to math.min(matches.length() - 1, limit)) {
      val translation = matches.getJSONObject(i)

      //Set the chunks for the resulting translation pair
      val chunkL1 = if (language == l1) chunk else Chunk.fromString(translation.getString("translation"))
      val chunkL2 = if (language == l1) Chunk.fromString(translation.getString("translation")) else chunk

      //Check the source of the translation:
      val source = translation.getString("created-by") match {
        case "MT!" => TranslationSource.ExternalMT
        case _ => TranslationSource.ExternalTM
      }

      //Use the quality metric as the prior score of the TP:
      //TODO: second score: "match"?

      val quality = translation.getString("quality") match {
        case "" => 0.0
        case s: String => s.toInt * 0.01
      }

      candidates += new ScoredTranslationPair(
        chunkL1,
        chunkL2,
        source,
        null,
        priorScore = quality
      )
    }

    candidates.toList
  }

}
