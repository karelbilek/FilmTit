//has to be run scala -cp ../src/server/target/server-0.1.jar split_lines.scala where/the/tits/are

import cz.filmtit.share.TitChunkSeparator

def remove_pipes (k: String) = {
        k.replaceAll("\\|", " ")
}


val newdir = args(0)+"_aligned"

for(filenm <- new java.io.File(args(0)).listFiles.map{_.getAbsolutePath()}.filter{_.endsWith(".txt")}) {
    val newfile = new java.io.PrintWriter(new java.io.File(filenm.replace(args(0), newdir)))
    println("doing "+filenm)
    
    try {
        scala.io.Source.fromFile(filenm).getLines.foreach {
        line => 
            try {
                    val Array(czech, english) = line.split("\t")
                    var czech_chunks = TitChunkSeparator.separate(czech, true) 
                    var english_chunks = TitChunkSeparator.separate(english, true)
                    
                    if (czech_chunks.size != english_chunks.size) {
                        czech_chunks = TitChunkSeparator.separate(czech, false) 
                        english_chunks = TitChunkSeparator.separate(english, false)
                    }
                    
                    if (czech_chunks.size == english_chunks.size) {
                            (0 to (czech_chunks.size - 1)).foreach {
                                i=>
                                newfile.println(remove_pipes(czech_chunks.get(i) + "\t"+ english_chunks.get(i)))
                            }
                    } else {
                        newfile.println(remove_pipes(czech + "\t"+ english))
                    }
            } catch {
                case e: MatchError => println("sucks - "+line);
            }
        }
    } catch {
        case e: java.nio.charset.MalformedInputException=>println("sucks");
    }
}
