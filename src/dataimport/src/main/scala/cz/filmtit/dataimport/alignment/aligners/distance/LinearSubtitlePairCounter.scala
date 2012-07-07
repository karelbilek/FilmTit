package cz.filmtit.dataimport.alignment.aligners.distance
import scala.Math._

/**
 * FilePairCounter that counts distance of two subtitles like
 * difference between the middles of timings
 * (0 if same times)
 */
class LinearSubtitlePairCounter extends FilePairCounter {

  /**
   * Counts distance of two subtitles like
   * difference between the middles of timings
   * (0 if same times)
   *
   * @param start1 Start of first chunk
   * @param start2 Start of second chunk
   * @param end1 end of first chunk
   * @param end2 end of second chunk
   * @return difference of the middles of timings
   */
    def timeDistance(start1:Long, start2:Long, end1:Long, end2:Long):Long = {
        return abs((start1+end1)/2 - (start2+end2)/2)
    }
}

