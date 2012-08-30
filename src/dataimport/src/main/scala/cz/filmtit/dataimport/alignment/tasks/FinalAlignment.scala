package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.core.Configuration
import cz.filmtit.share.Language
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.dataimport.alignment.aligners.distance.LinearSubtitlePairCounter
import cz.filmtit.dataimport.alignment.aligners.levensthein._
import java.io.File
import cz.filmtit.dataimport.alignment.model._
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._

/**
 * Task object, that just does the one, final alignment, without any second guesses
 */
object FinalAlignment {
  def main(args:Array[String]) {
    val c = new Configuration(args(0))
    val l1 = c.l1
    val l2 = c.l2
    val tolerance = 6000L

    val mapping = new SubtitleMapping(c, true)
    println("mapping done")
    
    
    val counter = new LevenstheinDistanceCounter(tolerance)


    val aligner = new Aligner(
      new LevenstheinSubtitleFileAlignment(l1,l2, counter),
      new LevenstheinChunkAlignment(l1, l2, tolerance),
      new LevenstheinGoodFilePairChooser(counter),
      c,
      l1, l2
    )



    println("before aligning")
    aligner.align(mapping)
    println("after aligning")
  }
}
