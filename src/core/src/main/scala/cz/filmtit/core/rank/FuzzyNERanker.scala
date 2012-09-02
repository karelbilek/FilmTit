package cz.filmtit.core.rank

import collection.mutable.ListBuffer
import cz.filmtit.share.{TranslationPair, MediaSource, Chunk}
import cz.filmtit.core.model.data.ChunkUtils.toAnnotatedString

/**
 * A ranker for Named Entities.
 *
 * @author Joachim Daiber
 */

class FuzzyNERanker extends BaseRanker {

  val lambdas = (0.95, 0.05)

  def getScoreNames: List[String] = List("edit_distance", "genre_match")
  def getScores(chunk: Chunk, mediaSource: MediaSource, pair: TranslationPair, totalCount: Int): List[Double] = List()


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
  TranslationPair = {

    /* We know that the two strings have the same NE annotations and that
     * the rests of the strings are equal.
     *
     * Check whether despite the NEs, the underlying surface forms are also
     * the same.
     */

    val matches: List[Int] = matchingSFs(pair.getChunkL1, chunk)

    //Add all the non-matching annotations to the chunk so that it can be
    //post-edited
    for ( i <- (0 until pair.getChunkL1.getAnnotations.size) ) {
      //if (!(matches contains i))
        //scoredPair.chunkL1.annotations += pair.chunkL1.annotations(i)
    }
    

    val l1annotatedString = toAnnotatedString(pair.getChunkL1, {(_, _) => "" })
    
    val distanceScore = 1.0 - ( l1annotatedString.length / chunk.getSurfaceForm.length.toFloat )

    pair.setScore((lambdas._1 * distanceScore) + (lambdas._2 * genreMatches(mediaSource, pair)))

    pair
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

    for (i <- (0 until chunk1.getAnnotations.size)) {
      val neFrom1 = chunk1.getAnnotations.get(i).getBegin
      val neTo1 = chunk1.getAnnotations.get(i).getEnd

      val neFrom2 = chunk2.getAnnotations.get(i).getBegin
      val neTo2 = chunk2.getAnnotations.get(i).getEnd

      if (chunk1.getSurfaceForm.substring(neFrom1, neTo1)
           equals chunk2.getSurfaceForm.substring(neFrom2, neTo2)) {
        matches += i
      }
    }

    matches.toList
  }


  def name = "Fuzzy ranker for Named Entities."

}
