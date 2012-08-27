package cz.filmtit.dataimport.alignment.aligners.trivial

import cz.filmtit.dataimport.alignment.model._
import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * Class for trivial chosing of file pairs
 * (just take all of them)
 */
class TrivialGoodFilePairChooser extends GoodFilePairChooser {

  /**
   * Trivial chosing of file pairs
   * @param pairs all file pairs
   * @return all file pairs
   */
    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):List[Pair[SubtitleFile, SubtitleFile]] = {
        return pairs.toList
    }

}
