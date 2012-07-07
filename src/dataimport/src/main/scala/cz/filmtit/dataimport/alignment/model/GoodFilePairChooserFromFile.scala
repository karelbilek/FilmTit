package cz.filmtit.dataimport.alignment.model

class GoodFilePairChooserFromFile(val map: Map[String, Pair[SubtitleFile, SubtitleFile]]) extends GoodFilePairChooser {

    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):Iterable[Pair[SubtitleFile, SubtitleFile]] = { 
       println("The map has "+map.size+" values.")
       return map.values 
    }


}
