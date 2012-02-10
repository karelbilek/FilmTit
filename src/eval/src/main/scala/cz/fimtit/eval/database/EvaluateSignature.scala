package cz.fimtit.eval.database

import io.Source
import java.io.File
import org.apache.commons.math.stat.StatUtils
import cz.filmtit.core.search.postgres.impl.FirstLetterStorage
import cz.filmtit.core.model.storage.{TranslationPairStorage, SignatureTranslationPairStorage}
import cz.filmtit.core.model.data.TranslationPair
import cz.filmtit.core.model.Language

/**
 * @author Joachim Daiber
 *
 *
 *
 */

object EvaluateSignature {

  val n = 2000000

  def evaluateSignature(storage: TranslationPairStorage,
                        pairs: Array[TranslationPair],
                        language: Language) {

    val counts = (((pairs map (p => storage.asInstanceOf[SignatureTranslationPairStorage]
      .signature(storage.chunkForLanguage(p, language), language))).toArray
      .groupBy(identity)) map({ case (x, y) => y.size.toDouble })).toArray

    val sigs = (pairs map (p => storage.asInstanceOf[SignatureTranslationPairStorage]
      .signature(storage.chunkForLanguage(p, language), language))).toArray

    println("=" * 35)
    println("Language: %s".format(language))
    println("=" * 35)

    println("Uniq chunks:   " + pairs.map(p =>
      storage.chunkForLanguage(p, language)).distinct.size / pairs.size.toFloat )
    println("Uniq sign.:    " + counts.size )

    println("\nChunks per Signature:")
    println("Exactly one:   " + counts.filter(_ == 1).size / counts.size.toFloat)
    println("Mean:          " + StatUtils.mean(counts) )
    println("Max:           " + StatUtils.max(counts) )
    println("SD:            " + math.sqrt(StatUtils.variance(counts)) )

    println("\n" * 2)
  }


  def evaluateSignatures(storage: TranslationPairStorage, dataFolder: File) {

    val pairs = ((dataFolder.listFiles flatMap (
      Source.fromFile(_).getLines().map( TranslationPair.fromString(_) )
        filter( _ != null ) ))
        take (if (n > 0) n else Integer.MAX_VALUE))

    evaluateSignature(storage, pairs, storage.l1)
    evaluateSignature(storage, pairs, storage.l2)

  }


  def main(args: Array[String]) {

    evaluateSignatures(new FirstLetterStorage(Language.en, Language.cz),
                       new File("/Users/jodaiber/Desktop/LCT/" +
                         "LCT W11:12/FilmTit/data/parallel/utf8"))
  }


}