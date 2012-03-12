package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseSignatureStorage
import cz.filmtit.core.model._
import cz.filmtit.core.Factory
import cz.filmtit.core.Utils.t2mapper
import collection.mutable.ListBuffer
import storage.Signature
import cz.filmtit.core.model.data.Chunk

/**
 * Translation pair storage using named entity types to identify names.
 *
 *
 *
 *
 * @author Joachim Daiber
 */

class NEStorage(l1: Language, l2: Language, readOnly: Boolean = true)
  extends BaseSignatureStorage(l1, l2, TranslationSource.InternalNE, "sign_ne",
    reversible = true, readOnly) {

  val (neL1, neL2) = (l1, l2) map { Factory.createNERecognizers(_) }

  override def signature(chunk: Chunk, language: Language): Signature = {

    //val chunk = sentence

    //Use the NE Recognizers for the given language to find names
    // in the Chunk:
    (if (language equals l1) neL1 else neL2) foreach { _.detect(chunk) }

    //Replace NEs in Chunk with their NE type (e.g. "<Person>"):
    if (chunk.annotations.size > 0) {
      removeOverlap(chunk)
      Signature.fromChunk(chunk)
    } else {
      //No annotations in the Chunk
      Signature.fromString(chunk.surfaceform)
    }
  }


  override def annotate(chunk: Chunk, signature: Signature) {
    chunk.annotations ++= signature.annotations
    println()
  }


  /**
   * Ensure there are no overlapping annotations.
   * If there are two annotations on the same substring, choose the
   * longer annotation.
   *
   * @param chunk annotated chunk
   */
  def removeOverlap(chunk: Chunk) {
    if (chunk.annotations.size > 1) {

      //Sor the annotations by their start and end
      val sorted = chunk.annotations.sortBy(pair => (pair._2, pair._3))
      chunk.annotations.clear()
      chunk.annotations ++= sorted

      val remove = ListBuffer[Int]()
      for (i <- (0 to chunk.annotations.size - 2)) {

        val (first, second, startFirst, endFirst, startSecond, endSecond) = (
          i, i+1,
          chunk.annotations(i)._2,
          chunk.annotations(i)._3,
          chunk.annotations(i + 1)._2,
          chunk.annotations(i + 1)._3
        )


        /* Matches for exactly the same areas:
         ------######------
         ------######------
        */
        if ((startSecond == startFirst) && (endSecond == endFirst))
          remove += first


        /* Matches where the start is equivalent but the second match
          is longer:
          ------########------
          ------###########---
        */
        else if ((startFirst == startSecond) && (endFirst < endSecond))
          remove += first


        /* Matches where the the start of the second is smaller than the end
           of the first.
          -----############-------
          ------##############----
        */
        else if (startSecond < endFirst)
          remove += second


        /* Matches where the second match is altogether smaller than the first:
          ------########------
          -------#####--------
        */
        else if ((startFirst < startSecond) && (endFirst < endSecond))
          remove += second


      }

      //Remove the annotations in reverse order (so that the offsets are still
      //valid):
      remove.reverse foreach { i => chunk.annotations.remove(i) }
    }
  }


  override def name = "Named Entity based storage"

}
