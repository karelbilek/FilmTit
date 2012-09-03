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

package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.core.Configuration
import cz.filmtit.dataimport.alignment.io.SubtitleFile
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter.readFilePairsFromFile
import cz.filmtit.share.Language
import cz.filmtit.dataimport.alignment.model.eval.FilePairAnnotator

/**
 * Task object for computing the results from AnnotateCorrectFilePairs
 */
object CheckAlignedFilePairsAgainstAnnotated {

  /**
   * Reads correct pairs that the user previously saved
   * @param file name of files
   * @param c configuration
   * @return map movie id => pair of correct files
   */
  def readCorrectPairsFromFile(file: java.io.File, c: Configuration):
    Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]] = {

    val iterable = readFilePairsFromFile(file, c, Language.CS, Language.EN, true)

    iterable.groupBy {
      _._1.filmID
    }
  }

  /**
   * Script for finding true positives - the pairs, that aligner connected, and it was in human annotated results
   * @param correct map from previous step
   * @param chosenMap those who aligner chose
   * @return true positive number
   */
  def countTruePositive(correct: Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]],
                        chosenMap: Map[String, Pair[SubtitleFile, SubtitleFile]]): Int =
    //first I filter the correct ones ant then I take the filtered map size.
    correct.filter {

      case (movie, correctPairs) =>
        if (correctPairs.head._1.fileNumber == 0 && correctPairs.head._2.fileNumber == 0) {
          false
        } else {
          val maybeChosenPair = chosenMap.get(movie)
          if (maybeChosenPair.isEmpty) {
            false
          } else {
            val chosenPair = maybeChosenPair.get
            //or some reason I swapped the pairs by mistake somewhere in the process. Let it be by now.
            val ex = correctPairs.exists {
              correctPair =>
                correctPair._1.fileNumber == chosenPair._2.fileNumber &&
                  correctPair._2.fileNumber == chosenPair._1.fileNumber
            }
            ex
          }
        }
    }.size


  /**
   * Counts my definition of precision
   * @param correct the correct ones
   * @param chosen the ones that user chose
   * @return precision as a number
   */
  def countPrecision(correct: Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]],
                     chosen: Iterable[Pair[SubtitleFile, SubtitleFile]]): Float = {

    val chosenMap = chosen.map {
      case Pair(f1, f2) => (f1.filmID, (f1, f2))
    }.toMap

    val truePositive = countTruePositive(correct, chosenMap).toFloat


    val retrieved = correct.filter {
      case (movie, correctPairs) =>
        chosenMap.get(movie).isDefined
    }.size


    truePositive.toFloat / retrieved
  }

  /**
   * Counts my definition of recall
   * @param correct the correct ones
   * @param chosen the ones that user chose
   * @return recall as a number
   */
  def countRecall(correct: Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]],
                  chosen: Iterable[Pair[SubtitleFile, SubtitleFile]]): Float = {
    val chosenMap = chosen.map {
      case Pair(f1, f2) => (f1.filmID, (f1, f2))
    }.toMap

    val truePositive:Float = countTruePositive(correct, chosenMap).toFloat

    val notNullCorrect = correct.filter {
      case (movie, correctPairs) =>
        correctPairs.head._1.fileNumber != 0 && correctPairs.head._2.fileNumber != 0
    }.size

    truePositive.toFloat / notNullCorrect
  }


  /**
   * Does the whole thing, I guess :)
   * @param args ignored
   */
  def main(args: Array[String]) {


      val c = new Configuration(args(0))
      val fileCorrect = new java.io.File(args(1))
      val fileEvaluated = new java.io.File(args(2))
      val correct = readCorrectPairsFromFile(fileCorrect, c)
      val evaluated = readFilePairsFromFile(fileEvaluated, c, Language.EN, Language.CS, true)
      val precision = countPrecision(correct, evaluated)
      val recall = countRecall(correct, evaluated)
      println("prec " + precision + " recall " + recall)

  }
}
