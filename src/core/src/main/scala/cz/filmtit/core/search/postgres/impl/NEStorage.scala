package cz.filmtit.core.search.postgres.impl

import cz.filmtit.core.search.postgres.BaseSignatureStorage
import cz.filmtit.core.model._
import cz.filmtit.core.Factory
import cz.filmtit.core.Utils.t2mapper
import collection.mutable.ListBuffer
import data.Chunk

/**
 * Translation pair storage using named entity types to identify names.
 *
 *
 *
 *
 * @author Joachim Daiber
 */

class NEStorage(l1: Language, l2: Language)
  extends BaseSignatureStorage(l1, l2, "sign_ne") {

  val (neL1, neL2) = (l1, l2) map { Factory.createNERecognizers(_) }

  override def signature(sentence: Chunk, language: Language): String = {

    var chunk = sentence

    //Use the NE Recognizers for the given language to find names
    // in the Chunk:
    (language match {
      case lang if (lang equals l1) => neL1
      case lang if (lang equals l2) => neL2
    }) foreach { _.detect(chunk) }

    //Replace NEs in Chunk with their NE type (e.g. "<Person>"):
    if (chunk.annotations.size > 0) {
      chunk = removeOverlap(chunk)
      chunk.toAnnotatedString({(neType, _) => "<%s>".format(neType) })
    } else {
      //No annotations in the Chunk
      chunk.surfaceform
    }
  }

  override def annotate(chunk: Chunk, signature: String): Chunk = {




    chunk
  }



  def removeOverlap(chunk: Chunk): Chunk = {
    if (chunk.annotations.size > 1) {
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


        /* Matches where the end of the first is greater than the start
          of the second.
          -----############-------
          ------##############----
        */
        else if (startSecond < endFirst)
          remove += second


      }
      remove.reverse foreach { i => chunk.annotations.remove(i) }
    }

    chunk
  }


  override def name = ""

}

object NEStorage {
  def main(args: Array[String]) {

    val storage: NEStorage = new NEStorage(Language.en, Language.cz)
    println(storage.signature("Mr. Peter Tosh is 69 years old and is from New York.",
      Language.en))
  }
}
