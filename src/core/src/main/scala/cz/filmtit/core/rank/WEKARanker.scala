package cz.filmtit.core.rank

import _root_.java.io.{FileInputStream, File}
import cz.filmtit.share.{TranslationPair, MediaSource, Chunk}
import weka.classifiers.Classifier
import weka.core._

/**
 * A ranker for translation pairs based on a WEKA classifier. The classifier is used to
 * classify each translation pair and the confidence/probability value returned by the
 * WEKA classifier is used as the score for each translation pair.
 *
 * @author Joachim Daiber
 */

abstract class WEKARanker(val modelFile: File)  extends BaseRanker {

  //Read the classifier from the external file.
  val classifier: Classifier = SerializationHelper.read(new FileInputStream(modelFile)).asInstanceOf[Classifier]
  val attributes = new FastVector()
  (getScoreNames ::: List("class")).foreach{ n: String => attributes.addElement(new Attribute(n)) }

  //Create a WEKA dataset
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

      try {
        val score = classifier.distributionForInstance(inst) match {
          case r: Array[Double] if r.length > 1  => r(1) //Result of classification
          case r: Array[Double] if r.length == 1 => r(0) //Result of regression
        }
        pair.setScore( score )

      } catch {
        //If there was an error with any of the scores, WEKA will throw a NullPointerException. In this rare case,
        //we must set the score to 0.0
        case e: NullPointerException => pair.setScore( 0.0 )
      }


    }

    pairs.sorted
  }

  def rankOne(chunk: Chunk, mediaSource: MediaSource,  pair: TranslationPair): TranslationPair = pair

}