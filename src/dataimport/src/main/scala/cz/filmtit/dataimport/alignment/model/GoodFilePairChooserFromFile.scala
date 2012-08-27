package cz.filmtit.dataimport.alignment.model

import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * Helper class - GoodFilePairChooser that just returns the pairs that are in the file
 * @param map Map loaded from the file
 */
class GoodFilePairChooserFromFile(val map: Map[String, Pair[SubtitleFile, SubtitleFile]])
  extends GoodFilePairChooser {

    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):
      Iterable[Pair[SubtitleFile, SubtitleFile]] = {
        println("The map has "+map.size+" values.")
        return map.values
    }


}
