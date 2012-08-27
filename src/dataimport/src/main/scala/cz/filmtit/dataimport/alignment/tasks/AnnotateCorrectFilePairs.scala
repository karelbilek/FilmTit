package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.core.Configuration
import cz.filmtit.dataimport.alignment.model.eval.FilePairAnnotator

/**
 * Task object for annotating the correct file pairs
 */
object AnnotateCorrectFilePairs {


  def main(args:Array[String]) {

      val c = new Configuration(args(0))
      val numberOfMovies = args(1).toInt
      val saveFile = args(2)
      val f = new FilePairAnnotator(c, numberOfMovies, saveFile)
      f.createCorrectPairs

  }

}
