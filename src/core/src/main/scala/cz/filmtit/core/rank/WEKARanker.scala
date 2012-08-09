package cz.filmtit.core.rank

import _root_.java.io.{FileInputStream, File}
import cz.filmtit.share.{TranslationPair, MediaSource, Chunk}
import weka.classifiers.Classifier
import weka.core._

/**
 * @author Joachim Daiber
 */

abstract class WEKARanker(val modelFile: File)  extends BaseRanker {

  val classifier: Classifier = SerializationHelper.read(new FileInputStream(modelFile)).asInstanceOf[Classifier]
  val attributes = new FastVector()
  (getScoreNames ::: List("class")).foreach{ n: String => attributes.addElement(new Attribute(n)) }

  val wekaPoints = new Instances("Dataset", attributes, 0)
  wekaPoints.setClassIndex(wekaPoints.numAttributes()-1)

  override def rank(chunk: Chunk, mediaSource: MediaSource, pairs: List[TranslationPair]): List[TranslationPair] = {

    val totalCount = pairs.map(_.getCount).sum
    pairs.foreach{ pair: TranslationPair =>
      val scores = getScores(chunk, mediaSource, pair, totalCount) ::: List(0.0)

      val inst = new Instance(scores.size)
      inst.setDataset(wekaPoints)
      scores.zipWithIndex.foreach{
        case (s, i) => inst.setValue(i, s)
      }

      val score = classifier.distributionForInstance(inst) match {
        case r: Array[Double] if r.length > 1  => r(1) //Result of classification
        case r: Array[Double] if r.length == 1 => r(0) //Result of regression
      }

      pair.setScore( score )
    }

    pairs.sorted
  }

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = pair

}