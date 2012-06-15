package cz.filmtit.dataimport.alignment.model

import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk


abstract class GoodFilePairChooser {

    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):List[Pair[SubtitleFile, SubtitleFile]];

}
