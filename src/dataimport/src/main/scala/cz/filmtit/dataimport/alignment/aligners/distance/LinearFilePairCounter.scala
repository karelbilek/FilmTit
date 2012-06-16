package cz.filmtit.dataimport.alignment.aligners.distance
import scala.Math._

class LinearFilePairCounter extends FilePairCounter {

    def timeDistance(start1:Long, start2:Long, end1:Long, end2:Long):Long = {
        return abs((start1+end1)/2 - (start2+end2)/2)
    }
}

