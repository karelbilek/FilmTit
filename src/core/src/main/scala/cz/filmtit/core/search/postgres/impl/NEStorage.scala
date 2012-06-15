package cz.filmtit.core.search.postgres.impl

import java.sql.Connection
import cz.filmtit.core.search.postgres.BaseSignatureStorage
import cz.filmtit.core.model._
import cz.filmtit.core.Utils.t2mapper
import collection.mutable.ListBuffer
import storage.Signature
import cz.filmtit.share._
import cz.filmtit.core.{Configuration, Factory}
import cz.filmtit.core.model.names.NERecognizer

import scala.collection.JavaConversions._

/**
 * Translation pair storage using named entity types to identify names.
 *
 *
 *
 *
 * @author Joachim Daiber
 */

class NEStorage(
  l1: Language, 
  l2: Language, 
  connection:Connection,
  neL1: List[NERecognizer],
  neL2: List[NERecognizer],
  useInMemoryDB: Boolean = false
) extends BaseSignatureStorage(
    l1,
    l2,
    TranslationSource.INTERNAL_NE,
    "sign_ne",
    connection,
    useInMemoryDB,
    reversible = true
  ) {


  override def signature(chunk: Chunk, language: Language): Signature = {

    //val chunk = sentence

    //Use the NE Recognizers for the given language to find names
    // in the Chunk:
    (if (language equals l1) neL1 else neL2) foreach { _.detect(chunk) }

    //Replace NEs in Chunk with their NE type (e.g. "<Person>"):
    if (chunk.getAnnotations.size > 0) {
      removeOverlap(chunk)
      Signature.fromChunk(chunk)
    } else {
      //No annotations in the Chunk
      Signature.fromString(chunk.getSurfaceForm)
    }
  }


  override def annotate(chunk: Chunk, signature: Signature) {
    //chunk.annotations ++= signature.annotations
    chunk.addAnnotations(signature.annotations)
  }


  /**
   * Ensure there are no overlapping annotations.
   * If there are two annotations on the same substring, choose the
   * longer annotation.
   *
   * @param chunk annotated chunk
   */
  def removeOverlap(chunk: Chunk) {
    if (chunk.getAnnotations.size > 1) {

      //Sor the annotations by their start and end
      val sorted = chunk.getAnnotations.sortBy(pair => (pair.getBegin, pair.getEnd))
      chunk.clearAnnotations()
      chunk.addAnnotations(sorted)

      val remove = ListBuffer[Int]()
      for (i <- (0 to chunk.getAnnotations.size - 2)) {

        val (first, second, startFirst, endFirst, startSecond, endSecond) = (
          i, i+1,
          chunk.getAnnotations.get(i).getBegin,
          chunk.getAnnotations.get(i).getEnd,
          chunk.getAnnotations.get(i + 1).getBegin,
          chunk.getAnnotations.get(i + 1).getEnd
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
      remove.reverse foreach { i => chunk.removeAnnotation(i) }
    }
  }


  override def name = "Named Entity based storage"

}
