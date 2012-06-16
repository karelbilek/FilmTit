package cz.filmtit.dataimport.alignment.aligners.distance

import cz.filmtit.dataimport.alignment.model._
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk
import scala.util.Sorting._
import cz.filmtit.dataimport.alignment.model.SubtitleFile


class DistanceGoodFilePairChooser(val counter:FilePairCounter) extends GoodFilePairChooser {

    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):List[Pair[SubtitleFile, SubtitleFile]] = {
        
        val distances:Iterable[Tuple3[Long, SubtitleFile, SubtitleFile]] = pairs.map {
            pair =>
                (counter.countFiles(pair._1, pair._1, true)._1, pair._1, pair._2)
        }

        //THIS WHOLE PART IS OVERCOMPLICATED, I am just confused by all the scala list-like stuff

        //wtf... scala is stupid sometimes
        val distArray = distances.toList.toArray
        
        quickSort(distArray) (Ordering.by[(Long,SubtitleFile, SubtitleFile), Long](_._1))

        val reverse = distArray.reverse

        //let's take first 6000
        //...I am not sure how to do it efficiently is scala :/
        val buf = new ListBuffer[Pair[SubtitleFile, SubtitleFile]]()

        (0 to 5999).foreach {
            i=>
            buf.append((distArray(i)._2, distArray(i)._3))
        }
        return buf.toList

    }

}
