package cz.filmtit.dataimport.alignment.aligners.trivial

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import  cz.filmtit.dataimport.alignment.model.Aligner
import  cz.filmtit.dataimport.alignment.model.Aligner._

/**
 * Object with main class for making a trivial alignment
 * (for baseline, say)
 */
object TrivialAlign {

  /**
   * Run the trivial aligning
   * @param args
   */
    def main(args: Array[String]) ={
        val config = new Configuration(args(0))
        val where = args(1)

        val aligner = new Aligner(
            new TrivialSubtitleFileAlignment(Language.EN, Language.CS),
            new TrivialChunkAlignment(Language.EN, Language.CS),
            new TrivialGoodFilePairChooser,
            config,
            Language.EN, Language.CS
        )

        writeFilePairsToFile(aligner.alignFiles(new SubtitleMapping(config)), new java.io.File(where));
    }
}
