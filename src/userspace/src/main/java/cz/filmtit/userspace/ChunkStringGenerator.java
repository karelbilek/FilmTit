package cz.filmtit.userspace;

import java.util.ArrayList;
import java.util.List;
import cz.filmtit.share.*;

public class ChunkStringGenerator {

    private List<TimedChunk> data ;
    private TimedChunk.FileType type;
    private double fps;

     public ChunkStringGenerator(List<TimedChunk> data, TimedChunk.FileType type, double fps) {
         this.data = data;
         this.type = type;
         this.fps = fps;
     }

     public String toString(){
        
        int actuallySaved = 0;
        StringBuilder builder = new StringBuilder();
        TimedChunk lastChunk = null;

        for (TimedChunk chunk : data) {
            
            if (chunk.sameTimeAs(lastChunk)) {
                lastChunk = lastChunk.joinWith(chunk, type);
            } else {
                if (lastChunk != null) {
                    actuallySaved++;
                    builder.append(lastChunk.getFileForm(type, actuallySaved, fps));
                }
                lastChunk = chunk;
            }
        }
       
        if (lastChunk != null) {
            actuallySaved++;
            builder.append(lastChunk.getFileForm(type, actuallySaved, fps));
        }
        return builder.toString();
    }
}
