package cz.filmtit.dataimport.alignment.trivial

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List


class TrivialSubtitleFileAlignment(l1:Language, l2:Language) extends SubtitleFileAlignment(l1,l2){

    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {
        return (filesL1(0), filesL2(0));
    }

}
