package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter.loadFilePairsToMap

/**
 * Helper object for writing heldout data. We do it by doing an alignment and then writing
 * the unaligned.
 */
object WriteHeldoutData {
  def writeHeldoutData(
                        alignment:SubtitleFileAlignment,
                        choser:GoodFilePairChooser,
                        mapping:SubtitleMapping,
                        conf:Configuration,
                        where:String) {

    val a = new Aligner(alignment, null, choser, conf, Language.EN, Language.CS);
    val alignedMovies = a.alignFiles(mapping).map{_._1.filmID}
    val nonalignedMovies = mapping.moviesWithSubs.toSet -- alignedMovies

    val nonalignedSubtitles = nonalignedMovies.map{
      m=>(m, mapping.getSubtitles(m).find{sf=>sf.language==Some(Language.EN)})}.filter{_._2.isDefined}
    val writer = new java.io.PrintWriter(new java.io.File(where))

    nonalignedSubtitles.foreach{
      case (movie, file)=>writer.println(movie+"\n"+file.get.fileNumber)
    }
    writer.close


  }

  def writeHeldoutData(where:String="heldout") {

    val c = new Configuration("configuration.xml")

    val mapping = new SubtitleMapping(c, false)
    val filename ="aligned"

    val file = new java.io.File(filename)
    val map = loadFilePairsToMap(file, c)

    writeHeldoutData(new SubtitleFileAlignmentFromFile(Language.EN, Language.CS, map), new GoodFilePairChooserFromFile(map), mapping,c, where)


  }

  def main(args:Array[String]) = writeHeldoutData("heldout")

}
