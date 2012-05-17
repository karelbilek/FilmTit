//has to be run scala -cp ../src/server/target/server-0.1.jar split_lines.scala where/the/tits/are

import cz.filmtit.share.TitChunkSeparator


for(filenm <- new java.io.File(args(0)).listFiles.map{_.getAbsolutePath()}.filter{_.endsWith(".txt")}) {
    val newfile = new java.io.PrintWriter(new java.io.File(filenm+"_aligned"))
    println("doing "+filenm)

    scala.io.Source.fromFile(filenm).getLines.foreach {
    line => 
    try {
            val Array(czech, english) = line.split("\t")
            val czech_chunks = TitChunkSeparator.separate(czech) 
            val english_chunks = TitChunkSeparator.separate(english)
            if (czech_chunks.size == english_chunks.size) {
                    (0 to (czech_chunks.size - 1)).foreach {
                        i=>
                        newfile.println(czech_chunks.get(i) + "\t"+ english_chunks.get(i))
                    }
            }
    } catch {
        case e: MatchError => println("sucks");
    }
    }

}
