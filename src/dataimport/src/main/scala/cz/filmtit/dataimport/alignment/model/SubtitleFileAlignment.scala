package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List


abstract class SubtitleFileAlignment(val l1:Language, val l2:Language) {

    def alignFiles(files:List[SubtitleFile]):Option[Pair[SubtitleFile, SubtitleFile]];

}
