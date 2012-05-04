package cz.fimtit.eval.database

import io.Source
import java.io.File
import org.apache.commons.math.stat.StatUtils
import cz.filmtit.core.model.storage.SignatureTranslationPairStorage
import cz.filmtit.core.search.postgres.impl.NEStorage
import cz.filmtit.share.{Language, TranslationPair}
import cz.filmtit.core.Configuration

/**
 * @author Joachim Daiber
 *
 *
 *
 */

object EvaluateSignature {

  val n = -1

  def evaluateSignature(
    storage: SignatureTranslationPairStorage,
    pairs: Array[TranslationPair],
    language: Language
  ) {

    val s = System.currentTimeMillis
    val counts = (
      (pairs map
        {
          p: TranslationPair =>
          storage.signature(storage.chunkForLanguage(p, language), language)
        })
        .toArray
        .groupBy(identity)
        .map({ case (x, y) => y.size.toDouble }))
        .toArray

    val sigTime = System.currentTimeMillis - s


    println("=" * 35)
    println("Language: %s".format(language))
    println("n = %d".format(n))
    println("=" * 35)

    println("Uniq chunks:   " + pairs.map(p =>
      storage.chunkForLanguage(p, language)).distinct.size / pairs.size.toFloat )
    println("Uniq sign.:    " + counts.size )

    println("\nChunks per Signature:")
    println("Exactly one:   " + counts.filter(_ == 1).size / counts.size.toFloat)
    println("Mean:          " + StatUtils.mean(counts) )
    println("Max:           " + StatUtils.max(counts) )
    println("SD:            " + math.sqrt(StatUtils.variance(counts)) )

    println("\nSignature function:")
    println("Mean signature creation time: %2.fms".format(sigTime/n.toFloat) )


    println("\n" * 2)
  }


  def evaluateSignatures(
    storage: SignatureTranslationPairStorage,
    dataFolder: File,
    languages: Set[Language]
  ) {

    val pairs = ((dataFolder.listFiles flatMap (
      Source.fromFile(_).getLines().map( TranslationPair.fromString(_) )
        filter( _ != null ) ))
        take (if (n > 0) n else Integer.MAX_VALUE))

    languages foreach {
      evaluateSignature(storage, pairs, _)
    }

  }


  def main(args: Array[String]) {
    val configuration = new Configuration(new File("configuration.xml"))

    evaluateSignatures(
      new NEStorage(Language.EN, Language.CS, configuration),
      new File("/Users/jodaiber/Desktop/LCT/LCT W11:12/FilmTit/data/parallel/utf8"),
      languages = Set(Language.EN)
    )
  }


}