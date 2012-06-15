package cz.filmtit.dataimport.alignment.model

import scala.collection.immutable.List
import cz.filmtit.share.parsing.UnprocessedChunk


abstract class GoodFilePairChooser {

    def choosePairs(pairs:List[Tuple3[SubtitleFile, SubtitleFile, List[Pair[UnprocessedChunk, UnprocessedChunk]]]]):List[Tuple3[SubtitleFile, SubtitleFile, List[Pair[UnprocessedChunk, UnprocessedChunk]]]];

}
