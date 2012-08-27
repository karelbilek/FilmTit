package cz.filmtit.dataimport.alignment.tasks

import cz.filmtit.dataimport.alignment.model.eval.LevAlignmentHistogram

object GetDataForLevenstheinHistogram {
  def main(args: Array[String]) {
    val hist = new LevAlignmentHistogram(args(0), args(1).toInt);
    hist.writeHistogram()
  }
}
