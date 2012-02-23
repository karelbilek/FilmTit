package cz.filmtit.core.rank

import cz.filmtit.core.model.data.{ScoredTranslationPair, Chunk, MediaSource, TranslationPair}
import cz.filmtit.core.model.annotation.ChunkAnnotation
import collection.mutable.ListBuffer
import cz.filmtit.core.model.data.Chunk._


/**
 * @author Joachim Daiber
 */

class FuzzyNERanker extends BaseRanker {

  val lambdas = (0.95, 0.05)


  /**
   * NE-based translation pairs should be ranked by their similarity
   * to the searched chunk. The best possible match differs only in
   * 1 NE, the next best in 2, etc.
   *
   * @param chunk Chunk for which we are looking for a translation
   * @param mediaSource information about the Movie/TV Show for which
   *                    we try to find a translation
   * @param pair the translation pair candidate
   * @return translation pair with score
   */
  def rankOne(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair):
  ScoredTranslationPair = {

    /* We know that the two strings have the same NE annotations and that
     * the rests of the strings are equal.
     *
     * Check whether despite the NEs, the underlying surface forms are also
     * the same.
     */

    val scoredPair = ScoredTranslationPair.fromTranslationPair(pair)
    val matches: List[Int] = matchingSFs(pair.chunkL1, chunk)

    //Add all the non-matching annotations to the chunk so that it can be
    //post-edited
    for ( i <- (0 until pair.chunkL1.annotations.size) ) {
      if (!(matches contains i))
        scoredPair.chunkL1.annotations += pair.chunkL1.annotations(i)
    }
    val distanceScore = 1.0 - ( scoredPair.chunkL1.toAnnotatedString({(_, _) => "" }).length / chunk.length.toFloat )
    scoredPair.score = (lambdas._1 * distanceScore) + (lambdas._2 * genreMatches(mediaSource, pair))

    scoredPair
  }


  /**
   * Return the indexes of all NEs which have the same surface form in both
   * chunks.
   *
   * @param chunk1 first chunk
   * @param chunk2 second chunk
   * @return
   */
  def matchingSFs(chunk1: Chunk, chunk2: Chunk): List[Int] = {

    val matches = ListBuffer[Int]()

    for (i <- (0 until chunk1.annotations.size)) {
      val (_, neFrom1, neTo1): (ChunkAnnotation, Int, Int) = chunk1.annotations(i)
      val (_, neFrom2, neTo2): (ChunkAnnotation, Int, Int) = chunk2.annotations(i)

      if (chunk1.surfaceform.substring(neFrom1, neTo1)
           equals chunk2.surfaceform.substring(neFrom2, neTo2)) {
        matches += i
      }
    }

    matches.toList
  }


  def name = "Fuzzy ranker for Named Entities."

}
