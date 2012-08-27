package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.aligners.distance.LinearSubtitlePairCounter
import cz.filmtit.dataimport.alignment.aligners.levensthein.LevenstheinChunkAlignment
import java.io.File
import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._

/**
 * Task object, that just does the one, final alignment, without any second guesses
 */
object FinalAlignment {
  def final_alignment() {
    val c = new Configuration("configuration.xml")
    val l1 = Language.EN
    val l2 = Language.CS

    val mapping = new SubtitleMapping(c, false)
    println("mapdan")
    val filename ="../alignment_file2file/leven"
    println("B")
    val alignment =  new LevenstheinChunkAlignment(l1, l2, 6000L)
    println("C")

    val file = new File(filename)
    println("D")
    val map = loadFilePairsToMap(file, c)

    println("pred alignerem")
    val aligner:Aligner = new Aligner(new SubtitleFileAlignmentFromFile(l1, l2, map), alignment, new GoodFilePairChooserFromFile(map), c, l1, l2)
    aligner.align(mapping)
    println("po nem")
  }
}
