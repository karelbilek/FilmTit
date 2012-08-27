package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.dataimport.alignment.model.ChunkAlignment
import cz.filmtit.core.Configuration
import java.io.File
import cz.filmtit.dataimport.alignment.model.eval.TMEvaluator
import cz.filmtit.dataimport.alignment.aligners.distance.{DistanceChunkAlignment, LinearSubtitlePairCounter}
import cz.filmtit.dataimport.alignment.aligners.trivial.TrivialChunkAlignment
import cz.filmtit.share.Language
import cz.filmtit.dataimport.alignment.aligners.levensthein.LevenstheinChunkAlignment

/**
 * Task object for annotating with toy TM
 */
object AnnotateWithToyTM {

  def main(args: Array[String]) {
    //the configuration is hardcoded right now
    //(it will not be run again anyway)
    val conf = new Configuration("configuration.xml")
    val l1 = conf.l1
    val l2 = conf.l1

    val cnt = new LinearSubtitlePairCounter

    println("starting");
    val ar:Array[Tuple3[String, ChunkAlignment, String]] = Array (
              ("../alignment_file2file/leven",  new LevenstheinChunkAlignment(l1, l2, 6000L), "../c/leven6k"),
              ("../alignment_file2file/leven",  new LevenstheinChunkAlignment(l1, l2, 600L), "../c/leven600"),
              ("../alignment_file2file/distance",  new LevenstheinChunkAlignment(l1, l2, 6000L), "../c/leven6k_d"),
              ("../alignment_file2file/distance12k",  new LevenstheinChunkAlignment(l1, l2, 6000L), "../c/leven6k_d12k"),
              ("../alignment_file2file/leven",  new DistanceChunkAlignment(l1, l2, cnt), "../c/distance_leven"),
              ("../alignment_file2file/distance",  new DistanceChunkAlignment(l1, l2, cnt), "../c/distance"),
              ("../alignment_file2file/distance12k",  new DistanceChunkAlignment(l1, l2, cnt), "../c/distance12k"),
              ("../alignment_file2file/trivial",  new TrivialChunkAlignment(l1, l2), "../c/trivial")
    )

    TMEvaluator.doComparison(ar)

  }

  //TMEvaluator.countPercentages for counting the percentages later
}
