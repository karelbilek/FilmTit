package cz.filmtit.dataimport.alignment.aligners.distance

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * A class for aligning file<->file, based on the distances of the chunks
 * It saves time because we need to try each file from l1 with each file from l2
 *
 * @constructor create a new DistanceSubtitleFileAlignment.
 * @param l1 first language
 * @param l2 second language
 * @param counter  how to count the distances
 */
class DistanceSubtitleFileAlignment(l1:Language, l2:Language, val counter:FilePairCounter) extends SubtitleFileAlignment(l1,l2){

  /**
   * Takes the best pair of files based on sum of distances of chunks
   * @param filesL1 list of files of first language
   * @param filesL2 list of files of second language
   * @return Pair of best subtitle files
   */
    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {
        val scores= new ListBuffer[Pair[Long, Pair[SubtitleFile, SubtitleFile]]];

        var bestPair:Pair[Long, Pair[SubtitleFile, SubtitleFile]] = (999999999L, (filesL1(0), filesL2(0))) 
        filesL1.foreach {
            file1=>
                filesL2.foreach{
                    file2=>
                        System.out.println("Counting another score...")
                        val score:Long = counter.countFiles(file1, file2)._1
                        if (score < bestPair._1) {
                            bestPair=((score, (file1, file2)))
                        }
                }
        }

        return bestPair._2

    }

}
