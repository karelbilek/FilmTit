package cz.filmtit.dataimport.alignment.aligners.trivial

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import  cz.filmtit.dataimport.alignment.model.Aligner

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
        val config = new Configuration("configuration.xml")
        val aligner = new Aligner(
            new TrivialSubtitleFileAlignment(Language.EN, Language.CS),
            new TrivialChunkAlignment(Language.EN, Language.CS),
            new TrivialGoodFilePairChooser,
            config
        )

        aligner.align(new SubtitleMapping(config));
    }
}
