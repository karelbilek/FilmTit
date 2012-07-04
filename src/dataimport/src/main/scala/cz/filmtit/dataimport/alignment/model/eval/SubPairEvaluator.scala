package cz.filmtit.dataimport.alignment.eval

import cz.filmtit.core.Configuration
import cz.filmtit.dataimport.alignment.model.SubtitleFile
import cz.filmtit.share.parsing.UnprocessedChunk
import collection.mutable.ListBuffer


class SubPairEvaluator(c:Configuration, chunks:Int) {

    def printFileEnds(f1:SubtitleFile, f2:SubtitleFile) {
       
       val chunks1 = f1.readChunks
       val chunks2 = f2.readChunks

       val indexes1 = (0 to chunks1.size-1).takeRight(chunks)
       val indexes2 = (0 to chunks2.size-1).takeRight(chunks)

       val f:(Int, UnprocessedChunk)=>Unit = {case (i, ch)=>println(i+"\t"+ch.toString)}
       
       println("F1\n===") 
       indexes1.foreach{
          i=>f(i, chunks1(i))
       }
       
       println("F2\n===") 
       indexes2.foreach{
          i=>f(i, chunks2(i))
       }
    }

    def readCorrectSubs(f1:SubtitleFile, f2:SubtitleFile):Iterable[Pair[Int, Int]] = {
        printFileEnds(f1, f2)
        
        val buf = new ListBuffer[Pair[Int, Int]]()
        var continue = true

        while (continue) {
            
            println("Say czech line, STOP to end")
            val f:String = readLine()
            if (f=="STOP") {continue=false}
            println("Say english line")
            val s:String = if (continue) {readLine()} else {""}
            
            if (continue) {
                buf.append((f.toInt, s.toInt))
            }
        }
        buf

    }

    def writeCorrectSubsToFile(subs:Map[String, Iterable[Pair[Int, Int]]], folder:java.io.File) = {
         
         import java.io._
         subs.foreach {
            case (movie, subs)=>
               val newFile = new File(folder, movie)
               val printWriter = new PrintWriter(newFile)
               subs.foreach {
                  case (int1, int2)=>
                      printWriter.println(int1+"\n"+int2);
               }
               printWriter.close
         }
    }

    
}
