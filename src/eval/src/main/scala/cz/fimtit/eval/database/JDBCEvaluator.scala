package cz.fimtit.eval.database

import cz.filmtit.core.model._

object JDBCEvaluator {
  def main(args: Array[String]) {
    val storage: TranslationPairStorage = new PostgresFulltextStorage()

    storage.initialize(
      scala.io.Source.fromFile("/Users/jodaiber/Desktop/src/FilmTit/dbms_experiments/corpus.1m.txt").getLines().map(
        line => { new TranslationPair(line.trim(), null) }
      ).filter(pair => pair.source != "" && pair.source.split(" ").size < 30)
    )

    storage.candidates("Major depressive disorder.")
  }
}