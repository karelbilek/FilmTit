package cz.filmtit.dataimport.alignment.model
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping

class SubtitleFileAlignmentFromFile(l1:Language, l2:Language, val map: Map[String, Pair[SubtitleFile, SubtitleFile]]) extends SubtitleFileAlignment(l1,l2) {

       def alignFiles(filesL1:List[SubtitleFile], filesL2:List[SubtitleFile]):Pair[SubtitleFile, SubtitleFile] = {
        if (map.contains(filesL1(0).filmID)) {
            return map(filesL1(0).filmID)
        } else {
            (filesL1(0), filesL2(0))
        }
    }

}
