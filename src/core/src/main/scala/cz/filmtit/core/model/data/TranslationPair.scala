package cz.filmtit.core.model.data

import cz.filmtit.core.model.TranslationSource


/**
 * @author Joachim Daiber
 *
 * TODO: is there a P(pair)?
 *
 */

class TranslationPair(
  val chunkL1: Chunk,
  val chunkL2: Chunk,
  val source: TranslationSource,
  var mediaSource: MediaSource
) {

  def this(chunkL1: Chunk, chunkL2: Chunk) {
    this(chunkL1, chunkL2, TranslationSource.Unknown, null)
  }

  override def toString = "TP(%s, %s, Source: %s)".format(
    chunkL1.toAnnotatedString( (c, s) => "["+s+"]" ),
    chunkL2.toAnnotatedString( (c, s) => "["+s+"]" ),
    source
  )

  
  def toExternalString: String = {

    val s: String = chunkL1.surfaceform + "\t" + chunkL2.surfaceform
    
    if (mediaSource != null)
      s + "\t" + mediaSource.genres.mkString(",")
    else
      s
  }
  
  def getStringL1 = chunkL1.surfaceform
  def getStringL2 = chunkL2.surfaceform

}

object TranslationPair {

  implicit def fromString(string: String): TranslationPair = {
    val splitString = string.trim().split("\t")

    splitString.length match {
      case 2 => new TranslationPair(splitString(0), splitString(1))
      case _ => null
    }

  }

}