package cz.filmtit.dataimport.alignment.model.eval

import collection.mutable.ListBuffer
import cz.filmtit.share.Language
import collection.mutable.HashMap
import cz.filmtit.dataimport.SubtitleMapping
import cz.filmtit.core.Configuration
import cz.filmtit.share.parsing.UnprocessedChunk
import cz.filmtit.dataimport.alignment.io.SubtitleFile
import cz.filmtit.dataimport.alignment.io.AlignedFilesWriter._

/**
 * Class for showing the user all files of a given movie, user then has to select the correct one.
 * @param c Configuration
 * @param numberOfMovies How many movies do we try?
 * @param placeToWrite Where to write the results
 */
class FilePairAnnotator(c: Configuration, numberOfMovies:Int, placeToWrite:String) {

  /**
   * classical subtitle mapping
   */
    lazy val mapping = new SubtitleMapping(c, true)


  /**
   *  "Randomly" take movies. The random is however without randomized seed, it should be
   *  the same every time.
   * @return List of movie IDs
   */
    lazy val random:Iterable[String] = scala.util.Random.shuffle(mapping.moviesWithSubs).take(numberOfMovies)

  /**
   * Prints some excerpts of one file.
   * @param file File to write excerpts from.
   */
    def printFileExcerpts(file:SubtitleFile) {
       println(file.filmID)
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

  /**
   * Asks user which file pairs (note the plural) from just printed files are correct
   * @param movie movie ID
   * @return list of correct file pairs
   */
    def askUserForFilePairs(movie:String):Iterable[Pair[SubtitleFile, SubtitleFile]] = {
        val subtitleFiles = mapping.getSubtitles(movie)

        val buf = new ListBuffer[Pair[SubtitleFile, SubtitleFile]]()

        val map = new HashMap[String,SubtitleFile]();
        subtitleFiles.foreach{f=>map(f.fileNumber)=f}

        val cz = subtitleFiles.filter{f=>f.language==Language.CS}
        val en = subtitleFiles.filter{f=>f.language==Language.EN}

        println("==cz")
        cz.foreach{printFileExcerpts}
         println("==en")
        en.foreach{printFileExcerpts}
       
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

   /**
    * Does the whole business. Prints the excerpts, ask for the right pairs, prints them to file.
    */
    def createCorrectPairs() {

        val printWriter = new java.io.PrintWriter(new java.io.FileWriter(new java.io.File(placeToWrite), true), true)   
        random.foreach{fn=>writeFilePairsToPrintWriter(askUserForFilePairs(fn), printWriter)};
    }

}


