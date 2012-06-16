package cz.filmtit.dataimport.alignment.aligners.levensthein

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

class LevenstheinSubtitleFileAlignment(l1:Language, l2:Language, val counter:LevenstheinDistanceCounter) extends SubtitleFileAlignment(l1,l2){

    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {

        val scores= new ListBuffer[Pair[Long, Pair[SubtitleFile, SubtitleFile]]];

        var bestPair:Pair[Int, Pair[SubtitleFile, SubtitleFile]] = (99999, (filesL1(0), filesL2(0))) 
        filesL1.foreach {
            file1=>
                filesL2.foreach{
                    file2=>
                        val score:Int = counter.count(file1, file2)
                        if (score < bestPair._1) {
                            bestPair=((score, (file1, file2)))
                        }
                }
        }

        return bestPair._2

    }

}
