package cz.filmtit.dataimport.alignment.model.eval

import collection.mutable.ListBuffer
import cz.filmtit.share.Language
import collection.mutable.HashMap
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.core.Configuration
import cz.filmtit.dataimport.alignment.model.SubtitleFile
import cz.filmtit.share.parsing.UnprocessedChunk

class FilePairEvaluator(c: Configuration, numberOfFiles:Int, placeToWrite:String) {
    val mapping = new SubtitleMapping(c)
    def random:Iterable[String] = scala.util.Random.shuffle(mapping.moviesWithSubs).take(numberOfFiles)

    def writeFileExcerpts(file:SubtitleFile) {
       println(file.file.getName)
       println("=========")
       println("[begin]");
       val chunks = file.readChunks
       
       val f = {ch:UnprocessedChunk=>println(ch.toString)}

       chunks.take(10).foreach(f)
       println("[middle]");
      
       val middle = chunks.size/2 - 5;
       chunks.slice(middle, middle+10).foreach(f)
       println("[end]");

       chunks.takeRight(10).foreach(f)

    }

    def readCorrectExcerpts(movie:String):Iterable[Pair[SubtitleFile, SubtitleFile]] = {
        val subtitleFiles = mapping.getSubtitles(movie).get;

        val buf = new ListBuffer[Pair[SubtitleFile, SubtitleFile]]()

        val map = new HashMap[String,SubtitleFile]();
        subtitleFiles.foreach{f=>map(f.fileNumber)=f}

        val cz = subtitleFiles.filter{f=>f.language==Language.CS}
        val en = subtitleFiles.filter{f=>f.language==Language.EN}

        println("==cz")
        cz.foreach{writeFileExcerpts}
         println("==en")
        en.foreach{writeFileExcerpts}
       
        var continue = true;
        while (continue) {
            
            println("Say czech file, STOP to end")
            val f:String = readLine()
            if (f=="STOP") {continue=false}
            println("Say english file, ignore at last one")
            val s:String = if (continue) {readLine()} else {""}
            
            if (continue) {
                buf.append((map(f), map(s)))
            }
        }
        buf
    }

    
    def createCorrectPairs() {
        import cz.filmtit.dataimport.alignment.model.Aligner.writeFilePairsToPrintWriter

        val printWriter = new java.io.PrintWriter(new java.io.FileWriter(new java.io.File(placeToWrite), true), true)   
        random.foreach{fn=>writeFilePairsToPrintWriter(readCorrectExcerpts(fn), printWriter)};
    }

}

object FilePairEvaluator {
    def readCorrectPairsFromFile(file:java.io.File, c:Configuration):Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]] = {
        import cz.filmtit.dataimport.alignment.model.Aligner.readFilePairsFromFile
        
        val iterable = readFilePairsFromFile(file, c, Language.CS, Language.EN, true)
        
        iterable.groupBy{_._1.filmID}
    }

    def countTruePositive(correct:Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]], 
                       chosenMap:Map[String, Pair[SubtitleFile, SubtitleFile]]) : Float = 
            correct.filter {
            
            case (movie, correctPairs) =>
                if (correctPairs.head._1.fileNumber==0  && correctPairs.head._2.fileNumber==0) {
                    false
                } else {
                    val maybeChosenPair = chosenMap.get(movie)
                    if (maybeChosenPair==None) {
                        false
                    } else {
                        val chosenPair = maybeChosenPair.get
                        //or some reason I swapped the pairs by mistake somewhere in the process. Let it be by now.
                        val ex = correctPairs.exists{correctPair => 
                            correctPair._1.fileNumber == chosenPair._2.fileNumber &&
                            correctPair._2.fileNumber == chosenPair._1.fileNumber
                        }
                        ex
                    }
                }        
        }.size

    def countPrecision(correct:Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]], 
                       chosen:Iterable[Pair[SubtitleFile, SubtitleFile]]) : Float = {
        
        val chosenMap = chosen.map{case Pair(f1,f2)=>(f1.filmID, (f1,f2))}.toMap

        val truePositive = countTruePositive(correct, chosenMap)


        val retrieved = correct.filter {
            case(movie, correctPairs) =>
                chosenMap.get(movie).isDefined
        }.size


        truePositive.toFloat / retrieved
    }

    def countRecall(correct:Map[String, Iterable[Pair[SubtitleFile, SubtitleFile]]], 
                       chosen:Iterable[Pair[SubtitleFile, SubtitleFile]]) : Float = {
        val chosenMap = chosen.map{case Pair(f1,f2)=>(f1.filmID, (f1,f2))}.toMap
        
        val truePositive = countTruePositive(correct, chosenMap)

        val notNullCorrect = correct.filter {
            case(movie, correctPairs) =>
                correctPairs.head._1.fileNumber!=0 && correctPairs.head._2.fileNumber!=0
        }.size

        truePositive.toFloat / notNullCorrect
    }

  
  def main(args: Array[String]) {
    val createPairs:Boolean = false
    if (createPairs) {
        val c = new Configuration(args(0))
        val f = new FilePairEvaluator(c, args(1).toInt, args(2))
        f.createCorrectPairs
    } else {
        import cz.filmtit.dataimport.alignment.model.Aligner.readFilePairsFromFile
        
        val c = new Configuration(args(0))
        val fileCorrect = new java.io.File(args(1))
        val fileEvaluated = new java.io.File(args(2))
        val correct = readCorrectPairsFromFile(fileCorrect, c)
        val evaluated = readFilePairsFromFile(fileEvaluated, c, Language.EN, Language.CS, true)
        val precision = countPrecision(correct, evaluated)
        val recall = countRecall(correct, evaluated)
        println("prec "+precision+" recall "+recall)
    }
  }  
}
