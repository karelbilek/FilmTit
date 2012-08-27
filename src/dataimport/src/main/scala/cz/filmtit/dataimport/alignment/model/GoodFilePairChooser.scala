package cz.filmtit.dataimport.alignment.model

import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.io.SubtitleFile

/**
 * An abstract object for determining whether to take file pairs that
 * are already aligned
 *
 */
abstract class GoodFilePairChooser {
  /**
   * Abstract method determining which file pairs to take
   * @param pairs all file pairs
   * @return the correct file pairs
   */
    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):Iterable[Pair[SubtitleFile, SubtitleFile]];

}
