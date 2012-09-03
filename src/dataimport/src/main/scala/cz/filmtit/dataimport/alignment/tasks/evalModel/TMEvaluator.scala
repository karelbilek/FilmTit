/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.dataimport.alignment.model.eval

import scala.collection.JavaConversions._
import cz.filmtit.dataimport.alignment.aligners.distance.DistanceChunkAlignment
import cz.filmtit.dataimport.alignment.aligners.distance.LinearSubtitlePairCounter
import cz.filmtit.dataimport.alignment.aligners.trivial.TrivialChunkAlignment
import cz.filmtit.dataimport.alignment.aligners.levensthein.LevenstheinChunkAlignment
import cz.filmtit.dataimport.database.Import
import cz.filmtit.dataimport.alignment.model.GoodFilePairChooserFromFile
import cz.filmtit.dataimport.alignment.model.SubtitleFileAlignmentFromFile
import cz.filmtit.dataimport.alignment.model.Aligner
import cz.filmtit.core.Configuration
import cz.filmtit.share.parsing.UnprocessedChunk
import collection.mutable.ListBuffer
import cz.filmtit.dataimport.alignment.model.ChunkAlignment
import java.io.File
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.share.Chunk
import cz.filmtit.share.MediaSource
import cz.filmtit.share.Language
import cz.filmtit.share.parsing.Parser
import cz.filmtit.dataimport.alignment.io.SubtitleFile
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._
//import cz.filmtit.share.parsing.Parser.processChunk

/**
 * Class for playing with the "toy" translation memory, and for asking user for the number of correct matches.
 * It works with a concrete ChunkAlignment, which then creates a corpus and then queries the corpus for some
 * strings.
 * The corpus does not contain the files that are asked.
 * Those strings are shown to the user.
 *
 * @param c configuration
 * @param alignedFiles file with the information about file alignment
 * @param chunkAlignment the ChunkAlignment class that will be used
 * @param numberOfTestedMovies how many movies do we test?
 */
class TMEvaluator(
                   val c:Configuration,
                   val alignedFiles:File,
                   val chunkAlignment:ChunkAlignment,
                   val numberOfTestedMovies:Int) {
    val l1 = c.l1 //en
    val l2 = c.l2 //cz

  /**
   *  "Randomly" take movies. The random is however without randomized seed, it should be
   *  the same every time.
   */
   lazy val tested:Seq[String] =
     scala.util.Random.shuffle(TMEvaluator.mapping.moviesWithSubs).take(numberOfTestedMovies).toSeq

  /**
   * Load all the filepairs from alignedFiles to map.
   */
   lazy val loadAlignedFiles:Map[String, Pair[SubtitleFile, SubtitleFile]] = loadFilePairsToMap(alignedFiles, c)

  /**
   * Load all the filepairs from alignedFiles to map, except for the one that we test
   */
   lazy val loadAlignedFilesExceptTested:Map[String, Pair[SubtitleFile, SubtitleFile]] =
     loadAlignedFiles -- tested

  /**
   * Files are aligned into the datafolder.
    */
   def alignFiles() {

        c.dataFolder.listFiles.foreach{_.delete()}

        val map = loadAlignedFilesExceptTested

        val aligner:Aligner = new Aligner(new SubtitleFileAlignmentFromFile(l1, l2, map), chunkAlignment, new GoodFilePairChooserFromFile(map), c, l1, l2)

        aligner.align(TMEvaluator.mapping)
   }

  /**
   * Helping procedure, that runs a function on every string pair on whole corpus
   * @param function function that is run
   */
   def forEachLine(function:Function2[String, String, Unit]) {
        c.dataFolder.listFiles.foreach{
            filename => 
        //I have no  clue why is the io.Source crashing=>rewriting as java reader
                import scala.collection.mutable.ListBuffer
                import java.io._
                
                val fstream:FileInputStream = new FileInputStream(filename)
                val in: DataInputStream  = new DataInputStream(fstream)
                val br: BufferedReader = new BufferedReader(new InputStreamReader(in))
                var strLine:String =""
                
                val reg = """(.*)\t(.*)""".r
                strLine = br.readLine
                while (strLine != null)   {
                    strLine match {
                        case reg(en,cz)=>
                                function(en,cz)
                    }
                    strLine = br.readLine
                    
                }
                in.close
         }
   }

  /**
   * Get count of the whole corpus
   * @return Number of lines in the whole corpus
   */
   def getPairCount():Int = {
       var i = 0
       forEachLine({(_,_)=>i+=1})
       i
   }

  /**
   * Finds pairs with the given sentence at the source side.
   * @param sourceSentencesToFind Sentences that I want to find (as strings)
   * @return Results of those sentences. In the exact same order.
   */
    def queryTMForStrings(sourceSentencesToFind:Seq[String]):Seq[Iterable[String]] = {
        val set = sourceSentencesToFind.toSet
            
        val resultBuffer: ListBuffer[Pair[String,String]] = new ListBuffer[Pair[String,String]]()
        forEachLine ({ (en,cz) =>
            if (set.contains(en)) {
                resultBuffer += ((en,cz))
            }
        })
               //to every string, a map of its translations
        val resMap:Map[String, Iterable[String]] = resultBuffer.groupBy{_._1}.mapValues{_.map{_._2}}
 
        sourceSentencesToFind.map{
            sourceSentence => if(resMap.contains(sourceSentence)){
                resMap(sourceSentence).take(30) //take 30 or lee
            } else {
                Iterable[String]() //empty if no match
            }
        }
    }

  /**
   * Loads subtitles from the tested set from given positions
   * @return Subtitles from given positions
   */
    def loadTestedFiles():Seq[String] = {
        val files:Seq[SubtitleFile] = tested.map {
           //take just the first english thing, who cares
           //(I have minBy there so it is deterministic)
           moviename=>
              TMEvaluator.mapping.getSubtitles(moviename).filter{_.language==l1}.minBy{_.fileNumber.toInt}
        }

        //load chunks from lines 60-70 (totally arbitrary)
        val unprocessedChunks:Seq[UnprocessedChunk] = files.flatMap {
            _.readChunks.slice(60,70)
        }


        val processedChunks:Seq[Chunk] = unprocessedChunks.flatMap {
            Parser.processChunk(_, 0, 0L, l1)
        }

        val res:Seq[String] = processedChunks.map {
            _.getSurfaceForm
        }
        res
    }



}

object TMEvaluator {


    //Hardcoded, because it is run only once anyway :)
    lazy val c = new Configuration("configuration.xml")
    lazy val mapping = new SubtitleMapping(c, false)

  /**
   * Ask the user  for every sentence
   * @param source  sequence of source strings (those were matched)
   * @param targets results of the match, now we have to annotate them
   * @return sequence of scores. +n== n correct, I decided that
   *         -1 == everything correct, -n == everything but (n-1) correct
   *               for better reading
   */
  def askUserForAnnotations(source:Seq[String], targets:Seq[Iterable[String]]):Seq[Int] = {
    (0 to source.size-1).flatMap {
      i=>
        val input_real = if (targets(i).size > 0) {

          println("=========")
          println(source(i))
          println("--------")
          targets(i).foreach{println(_)}
          println("========")
          var done = false
          var input = 0
          while (!done){
            println("enter")

            try {
              input = readLine.toInt
              done = true
            } catch {
              case e:Exception=>done = false
            }
          }
          input
        } else {
          0
        }


        Array(input_real, targets(i).size)
    }
  }


  /**
   * Save basically any kind of sequence of int to file. Very simple method.
   * @param where where to save
   * @param what what to save
   */
  def saveCountInfo(where:File, what:Seq[Int]) {
    val writer = new java.io.PrintWriter(where)
    what.foreach{writer.println(_)}
    writer.close
  }

  /**
   * Actually does the whole magics and saves it to file.
   * @param descriptions list of tuples of:
   *                     - string for file2file alignment (read from file)
   *                     - chunk alignment
   *                     - string for where to save the results
   */
  def doComparison(descriptions: Iterable[Tuple3[String, ChunkAlignment, String]]) {
    descriptions.foreach {

      case (file2file, chunk2chunk, results) =>
        println("doing another one")
        val evaluator = new TMEvaluator(new Configuration("configuration.xml"), new File(file2file), chunk2chunk, 30)

        evaluator.alignFiles
        println("alignment done")
        //if (false) {
          //testing recall/precision by printing/returning stuff by user
          val testedS = evaluator.loadTestedFiles
          val testedT = evaluator.queryTMForStrings(testedS)
          saveCountInfo(new File(results), askUserForAnnotations(testedS, testedT))
        //} else {
          //saveCountInfo(new File(results), Seq[Int](evaluator.getPairCount))

        //}
    }
  }

  /**
   * Counts the precision, coverage and mean, based on the results.
   */
    def countPercentages() {
        //hardcoded, but I run it only once anyway
        val res = new java.io.File("../c")

        res.listFiles.foreach {
            name=>
            var pr_number = 0;
            var predcitam = true
            var all_subs = 0
            var covered_subs = 0
            var all_sent = 0
            var all_correct = 0
            io.Source.fromFile(name).getLines.foreach {
                line=>
        
                val loaded_int = line.toInt
                if (predcitam) {
                    pr_number = loaded_int
                    predcitam = false
                } else {
                    val titlessent = loaded_int
                    val titlescorrect = if (pr_number>=0) {pr_number} else {titlessent+pr_number+1}
                    all_subs+=1
                    if (titlescorrect>0) {covered_subs+=1}
                    
                    all_correct+=titlescorrect
                    all_sent+=titlessent
                    //if (titlescorrect>titlessent) {println("WTF - "+titlescorrect+" - "+titlessent+ "- filename"+mediaSourceID)}
                    predcitam=true
                }
                    
            }
            
            val precision = all_correct.toFloat/all_sent
            val coverage = covered_subs.toFloat/all_subs
            val harmean = 2*precision*coverage/(precision+coverage)

            println(name+"\t "+(precision*100).toInt+"\t"+(coverage*100).toInt+"\t"+(harmean*100).toInt)

        } 
    }



} 
