package cz.filmtit.dataimport.alignment.aligners.trivial

import cz.filmtit.dataimport.alignment.model._
import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk


class TrivialGoodFilePairChooser extends GoodFilePairChooser {

    def choosePairs(pairs:Iterable[Pair[SubtitleFile, SubtitleFile]]):List[Pair[SubtitleFile, SubtitleFile]] = {
        return pairs.toList
    }

}
