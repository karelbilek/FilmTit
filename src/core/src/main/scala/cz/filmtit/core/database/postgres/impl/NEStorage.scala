package cz.filmtit.core.database.postgres.impl

import cz.filmtit.core.database.postgres.BaseSignatureStorage
import cz.filmtit.core.model.Language.Language
import cz.filmtit.core.model._
import cz.filmtit.core.factory.Factory
import cz.filmtit.core.Configuration
import cz.filmtit.core.Utils.t2mapper
import collection.mutable.ListBuffer
import names.{NEType, NERecognizer}

/**
 * @author Joachim Daiber
 *
 */

class NEStorage(l1: Language, l2: Language)
  extends BaseSignatureStorage(l1, l2, "sign_ne") {

  object NERecognizers {
    val (neL1, neL2) = (l1, l2) map {
      l => Configuration.neRecognizers.get(l) match {
        case Some(recognizers) => recognizers map {
          pair => {
            val (neType, modelFile) = pair
            Factory.createNERecognizer(neType, l, modelFile)
          }
        }
        case None => List()
      }
    }
  }

  override def signature(sentence: Chunk, language: Language): String = {

    var chunk = sentence

    //Use the NE Recognizers for the given language to find names
    // in the Chunk:
    (language match {
      case lang if (lang equals l1) => NERecognizers.neL1
      case lang if (lang equals l2) => NERecognizers.neL2
    }).asInstanceOf[List[NERecognizer]] foreach {
      ner => chunk = ner.detect(chunk)
    }

    //Replace NEs in Chunk with their NE type (e.g. "<Person>"):
    if (chunk.annotations.size > 0) {
      if (chunk.annotations.size > 1) {
        val sorted = chunk.annotations.sortBy(pair => (pair._2, pair._3))
        chunk.annotations.clear()
        chunk.annotations ++= sorted

        val remove = ListBuffer[Int]()
        for(i <- (0 to chunk.annotations.size - 2)) {

          //Remove matches for exactly the same areas:
          if ((chunk.annotations(i + 1)._2 == chunk.annotations(i)._2) &&
            (chunk.annotations(i + 1)._3 == chunk.annotations(i)._3))
            remove += i

          else if ((chunk.annotations(i)._2 == chunk.annotations(i + 1)._2) &&
            (chunk.annotations(i)._3 < chunk.annotations(i + 1)._3))
            remove += i

          //Remove matches where the match i is smaller than match i+1
          else if (chunk.annotations(i)._3 > chunk.annotations(i + 1)._2)
            remove += i + 1


        }
        remove.reverse foreach { i => chunk.annotations.remove(i) }
      }

      annotationsToString(chunk)
    } else {
      chunk.surfaceform
    }
  }

  def annotationsToString(chunk: Chunk): String = {
    var lastOffset = 0
    (chunk.annotations.toList map {
      triple => {
        val (neType, from, to) = triple
        "%s<%s>".format(
        chunk.surfaceform.substring(lastOffset, from),
        neType,
        { lastOffset = to }
        )
      }
    }).mkString + chunk.surfaceform.substring(lastOffset)
  }


  override def name = ""

}

object NEStorage {
  def main(args: Array[String]) {

    val storage: NEStorage = new NEStorage(Language.en, Language.cz)
    println(storage.signature("Mr . Peter Tosh is 69 years old and own Apple Inc.",
      Language.en))
  }
}
