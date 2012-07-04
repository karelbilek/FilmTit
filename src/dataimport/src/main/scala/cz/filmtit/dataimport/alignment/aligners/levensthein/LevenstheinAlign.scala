package cz.filmtit.dataimport.alignment.aligners.levensthein

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import  cz.filmtit.dataimport.alignment.model.Aligner
import  cz.filmtit.dataimport.alignment.model.Aligner._

/**
 * Object with main class for making a distance-based alignment
 */
object LevenstheinAlign {


  /**
   * Run the distance-based linear aligning
   * @param args
   */
    def main(args: Array[String]) ={
        val tolerance = 6000L
        
        val config = new Configuration(args(0))
        val where = args(1)

        val counter = new LevenstheinDistanceCounter(tolerance)
        

        val aligner = new Aligner(
            new LevenstheinSubtitleFileAlignment(Language.EN, Language.CS, counter),
            new LevenstheinChunkAlignment(Language.EN, Language.CS, tolerance),
            new LevenstheinGoodFilePairChooser(counter),
            config,
            Language.EN, Language.CS
        )

//        aligner.align(new SubtitleMapping(config));
        
        writeFilePairsToFile(aligner.alignFiles(new SubtitleMapping(config)), new java.io.File(where));
    }
}