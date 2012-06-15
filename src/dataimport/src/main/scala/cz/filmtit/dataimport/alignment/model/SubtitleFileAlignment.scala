package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.Language
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer


abstract class SubtitleFileAlignment(val l1:Language, val l2:Language) {

    def alignFiles(files:List[SubtitleFile]):Option[Pair[SubtitleFile, SubtitleFile]] = {
        val filesL1 = new ListBuffer[SubtitleFile]()
        val filesL2 = new ListBuffer[SubtitleFile]()
        files.foreach{f=>
            /*f.language match {
                case l1 => filesL1.append(f);
                case l2 => filesL2.append(f);
                case _ => return None;
            }*/
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

    def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile];

}
