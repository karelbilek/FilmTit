package cz.fimtit.eval.database

import cz.filmtit.core.model._
import collection.mutable.ListBuffer
import cz.filmtit.core.database.postgres.impl.{FulltextStorage, FirstLetterStorage, TrigramStorage}
import cz.filmtit.core.database.postgres.BaseStorage

object EvaluateJDBC {

  var storages: ListBuffer[TranslationPairStorage] = ListBuffer[TranslationPairStorage]()

  storages += new FulltextStorage(Language.en, Language.cz)
  storages.last.asInstanceOf[BaseStorage].chunkTable = "sentences_fulltext"

  storages += new FirstLetterStorage(Language.en, Language.cz)
  storages.last.asInstanceOf[BaseStorage].chunkTable = "sentences_firstletter"

  storages += new TrigramStorage(Language.en, Language.cz)
  storages.last.asInstanceOf[BaseStorage].chunkTable = "sentences_trigram"


  def fill() {
    //storages foreach (_.initialize(
    //    //Read our format: scala.io.Source.fromFile(...).getLines().asInstanceOf[Iterator[TranslationPair]]
    //    //scala.io.Source.fromFile("/Users/jodaiber/Desktop/src/FilmTit/dbms_experiments/corpus.1m.txt").getLines()
    //    scala.io.Source.fromFile("").getLines().asInstanceOf[Iterator[TranslationPair]].map(
    //      line => { new TranslationPair(line, null) }
    //    ).filter(pair => pair.source != "" && pair.source.split(" ").size < 30)
    //  , true)
    //)
  }

  def query(sentence: String) {
    storages foreach (storage => {
      println("\n\nCandidates for storage " + storage.name)
      val start = System.currentTimeMillis()
      storage.candidates(sentence, Language.en)
      val end = System.currentTimeMillis()
      val elapsedTime = end - start
      println("Retrieval took %d ms.".format(elapsedTime))
    })
  }

  def main(args: Array[String]) {
    //fill()

    println("=> Neurology is the study of the brain.")
    query("Neurology is the study of the brain.")
    query("Another option is to switch to the atypical antidepressant bupropion.")

  }
}