package cz.filmtit.dataimport.alignment.aligners.distance

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer


class DistanceSubtitleFileAlignment(l1:Language, l2:Language, val counter:FilePairCounter) extends SubtitleFileAlignment(l1,l2){

    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {
        val scores= new ListBuffer[Pair[Long, Pair[SubtitleFile, SubtitleFile]]];

        var bestPair:Pair[Long, Pair[SubtitleFile, SubtitleFile]] = (999999999L, (filesL1(0), filesL2(0))) 
        filesL1.foreach {
            file1=>
                filesL2.foreach{
                    file2=>
                        val score:Long = counter.countFiles(file1, file2, false, bestPair._1)._1
                        if (score < bestPair._1) {
                            bestPair=((score, (file1, file2)))
                        }
                }
        }

        return bestPair._2

    }

}
