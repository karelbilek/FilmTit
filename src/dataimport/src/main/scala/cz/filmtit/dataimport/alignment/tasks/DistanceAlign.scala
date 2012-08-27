package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.model.Aligner
import cz.filmtit.dataimport.alignment.aligners.distance.{DistanceGoodFilePairChooser, DistanceChunkAlignment, DistanceSubtitleFileAlignment, LinearSubtitlePairCounter}
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._


/**
 * Object with main class for making a distance-based alignment
 */
object DistanceAlign {


  /**
   * Run the distance-based linear aligning
   * @param args
   */
  def main(args: Array[String]) = {
    val count = 12000
    val config = new Configuration(args(0))
    val where = args(1)

    val counter = new LinearSubtitlePairCounter

    val aligner = new Aligner(
      new DistanceSubtitleFileAlignment(Language.EN, Language.CS, counter),
      new DistanceChunkAlignment(Language.EN, Language.CS, counter),
      new DistanceGoodFilePairChooser(counter, count),
      config,
      Language.EN, Language.CS
    )


    writeFilePairsToFile(aligner.alignFiles(new SubtitleMapping(config, true)), new java.io.File(where));
  }
}
