package cz.filmtit.dataimport.alignment.aligners.levensthein

import cz.filmtit.dataimport.alignment.model._
import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk

class LevenstheinGoodFilePairChooser(counter:LevenstheinDistanceCounter) extends GoodFilePairChooser {


    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):List[Pair[SubtitleFile, SubtitleFile]] = {
        
        return pairs.filter {
            pair=>
              counter.count(pair._1, pair._2)<30 
        }.toList
    }

}
