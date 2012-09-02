package cz.filmtit.share;

import java.util.List;

/**
 *  Wrapper TimedChunk for generating timed chunk to export format.
 */

public class ChunkStringGenerator {

    /**
     *  Document to exported
     */
    private Document document;
    /**
     *  Format type to export
     */
    private TimedChunk.FileType type;
    /**
     * Count frame per sec - speed screening of film
     */
    private double fps;

    /**
     * Interface for TimedChunk converter which has three anonymus implementations.
     * in  SOURCE_SIDE, TARGET_SIDE_WITH_THROWBACK, TARGET_SIDE
     */
     public interface ResultToChunkConverter {
        public TimedChunk getChunk(TranslationResult t);
     }

    /**
     *  Converting
     */
    private ResultToChunkConverter converter;

    /**
     * Creates new instance
     * @param document  document to generate
     * @param type type format export
     * @param fps  frame per second
     * @param converter chunk converter
     */
    public ChunkStringGenerator(Document document, TimedChunk.FileType type, double fps, ResultToChunkConverter converter) {

         this.document = document;
         this.type = type;
         this.fps = fps;
         this.converter = converter;
     }

                   //will not care about time

    /**
     * Creates text from subset of TranslationResults
     * @param results  Subset of TranslationResults
     * @param converter
     * @return
     */
     public static String listWithSameTimeToString(List<TranslationResult> results, ResultToChunkConverter converter) {
        if (results == null || results.size()==0) {
            return "";
        }

        TimedChunk res=null;
        for (TranslationResult r:results) {
            TimedChunk chunk = converter.getChunk(r);
            if (chunk!=null) {
                if (res == null) {
                    res = chunk;
                } else {
                    res = res.joinWith(chunk, TimedChunk.FileType.SRT);
                }
            }
        }
        if (res == null) {
            return "";
        }
        return res.getFormatedForm("- ", "\n");
     }

    /**
     *  Generates whole document in selected format
     * @return  String represents whole file
     */
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


    /**
     * Converter gets origin chunk
     */
    public static ResultToChunkConverter SOURCE_SIDE = new ResultToChunkConverter() {
        @Override
        public TimedChunk getChunk(TranslationResult result) {
             return result.getSourceChunk();
        }
    };

    /**
     * If chunk was traslated gets translated chunk otherwise origin chunk
     */
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

    /**
     * Converter which gets translated chunk
     */
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
