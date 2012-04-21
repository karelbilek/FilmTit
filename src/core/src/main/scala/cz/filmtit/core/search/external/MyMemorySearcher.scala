package cz.filmtit.core.search.external

import io.Source
import java.net.URLEncoder
import org.json.{JSONArray, JSONObject}
import cz.filmtit.core.model.TranslationPairSearcher
import collection.mutable.ListBuffer
import cz.filmtit.core.model.data.AnnotatedChunk
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

  val limit = 5
  val apiURL = (l1: String, l2: String, text: String) =>
    "http://mymemory.translated.net/api/get?langpair=%s|%s&q=%s"
      .format(l1, l2, URLEncoder.encode(text, "utf-8"))

  def candidates(chunk: Chunk, language: Language): List[TranslationPair] = {

    val apiResponse = new JSONObject(
      Source.fromURL(
        apiURL(language.getCode, {if (language == l1) l2.getCode else l1.getCode}, chunk.getSurfaceform)
      ).getLines().next()
    )

    //TODO: add exception handling!

    val candidates = ListBuffer[TranslationPair]()
    val matches: JSONArray = apiResponse.getJSONArray("matches")

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

}
