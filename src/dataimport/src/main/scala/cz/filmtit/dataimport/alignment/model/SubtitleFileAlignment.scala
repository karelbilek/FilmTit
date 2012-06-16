package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer

/** An abstract object for aligning file to file
 *
 * @constructor create a new SubtitleFileAlignment
 * @param l1 first language
 * @param l2 second language
 */
abstract class SubtitleFileAlignment(val l1:Language, val l2:Language) {

  /**
   * Aligns file to file
   * @param files list of files of both languages
   * @return Either None if there is no file of either language or third language, or
   *         returns the pair of subtitle files
   */
    def alignFiles(files:List[SubtitleFile]):Option[Pair[SubtitleFile, SubtitleFile]] = {
        val filesL1 = new ListBuffer[SubtitleFile]()
        val filesL2 = new ListBuffer[SubtitleFile]()
        files.foreach{f=>

            if (f.language == l1) {
                filesL1.append(f);
            } else if (f.language == l2) {
                filesL2.append(f);
            } else {
                return None
            }
        }
        
        if (filesL1.isEmpty) return None
        if (filesL2.isEmpty) return None
        
        Some(alignFiles(filesL1.toList, filesL2.toList))

    }

  /**
   * Aligns file to file, abstract method
   * @param filesL1 list of files of first language
   * @param filesL2 list of files of second language
   * @return Pair of best subtitle files
   */
    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile];

}
