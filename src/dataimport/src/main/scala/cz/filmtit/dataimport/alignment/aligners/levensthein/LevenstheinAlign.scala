package cz.filmtit.dataimport.alignment.aligners.levensthein

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import  cz.filmtit.dataimport.alignment.model.Aligner
import  cz.filmtit.dataimport.alignment.model.Aligner._
import cz.filmtit.dataimport.alignment.model.SubtitleFile

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
        val whereHeldout = args(2)

        val counter = new LevenstheinDistanceCounter(tolerance)
        

        val aligner = new Aligner(
            new LevenstheinSubtitleFileAlignment(Language.EN, Language.CS, counter),
            new LevenstheinChunkAlignment(Language.EN, Language.CS, tolerance),
            new LevenstheinGoodFilePairChooser(counter),
            config,
            Language.EN, Language.CS
        )

//        aligner.align(new SubtitleMapping(config));
        
        val mapping = new SubtitleMapping(config);
        println("mapping dan")
        val pairs = aligner.alignFiles(mapping)
        println("alignment dan")
        writeFilePairsToFile(pairs, new java.io.File(where))
        
        
        val nonalignedMovies:Iterable[String] = mapping.moviesWithSubsEn.toSet -- pairs.map{_._1.filmID}
        
        val nonalignedSubtitles = nonalignedMovies.map{m=>
            val allf:Iterable[SubtitleFile] = mapping.getSubtitles(m).get
            val en:Option[SubtitleFile] = allf.find{sf:SubtitleFile=>sf.language == Some(Language.EN)}
            (m, en)
        }.filter{_._2.isDefined}
        val writer = new java.io.PrintWriter(new java.io.File(whereHeldout))

        nonalignedSubtitles.foreach{
            case (movie, file)=>writer.println(movie+"\t"+file.get.fileNumber)
        }
        writer.close

        

    }
}
