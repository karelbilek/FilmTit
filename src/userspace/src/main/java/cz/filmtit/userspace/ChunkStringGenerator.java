package cz.filmtit.userspace;

import java.util.ArrayList;
import java.util.List;
import cz.filmtit.share.*;

//this is just sort of wrapper
//most of the functionality is in TimedChunk
public class ChunkStringGenerator {

    private Document document;
    private TimedChunk.FileType type;
    private double fps;

     public interface ResultToChunkConverter {
        public TimedChunk getChunk(TranslationResult t);
     } 
     private ResultToChunkConverter converter;

    public ChunkStringGenerator(Document document, TimedChunk.FileType type, double fps, ResultToChunkConverter converter) {
         this.document = document;
         this.type = type;
         this.fps = fps;
         this.converter = converter;
     }

     public String toString(){
        
        int actuallySaved = 0;
        StringBuilder builder = new StringBuilder();
        TimedChunk lastChunk = null;

        for (TranslationResult tr: document.getSortedTranslationResults()) {
            TimedChunk chunk = converter.getChunk(tr);
            
            if (chunk != null) {
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
        }
       
        if (lastChunk != null) {
            actuallySaved++;
            builder.append(lastChunk.getFileForm(type, actuallySaved, fps));
        }
        return builder.toString();
    }

    public static ResultToChunkConverter SOURCE_SIDE = new ResultToChunkConverter() {
        @Override
        public TimedChunk getChunk(TranslationResult result) {
             return result.getSourceChunk();
        }
    };

    public static ResultToChunkConverter TARGET_SIDE_WITH_THROWBACK = new ResultToChunkConverter() {
        @Override
        public TimedChunk getChunk(TranslationResult result) {
            boolean isDialogue = result.getSourceChunk().isDialogue();
            
            if (result.getUserTranslation()!=null && !result.getUserTranslation().equals("")) {
                return result.getUserTranslationAsChunk(isDialogue);
            } else {
                return result.getSourceChunk();
            }
        }
    };
    
    public static ResultToChunkConverter TARGET_SIDE = new ResultToChunkConverter() {
        @Override
        public TimedChunk getChunk(TranslationResult result) {
            boolean isDialogue = result.getSourceChunk().isDialogue();

            if (result.getUserTranslation()!=null && !result.getUserTranslation().equals("")) {
                return result.getUserTranslationAsChunk(isDialogue);
            } else {
                return null;
            }
        }
    };
}
