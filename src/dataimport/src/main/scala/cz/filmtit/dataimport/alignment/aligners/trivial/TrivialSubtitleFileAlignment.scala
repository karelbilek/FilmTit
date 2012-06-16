package cz.filmtit.dataimport.alignment.aligners.trivial

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.share.Language
import scala.collection.immutable.List

/**
 * Class for trivial file<->file alignment (just take first one from all)
 *
 * @param l1 first language
 * @param l2 second language
 */
class TrivialSubtitleFileAlignment(l1:Language, l2:Language) extends SubtitleFileAlignment(l1,l2){

  /**
   * Trivial aligning of files (just take first one from both languages)
   * @param filesL1 list of files of first language
   * @param filesL2 list of files of second language
   * @return Pair of best subtitle files
   */
    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {
        return (filesL1(0), filesL2(0));
    }

}
