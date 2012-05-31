package cz.filmtit.share.tests;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 17.5.12
 * Time: 6:31
 * To change this template use File | Settings | File Templates.
 */


import cz.filmtit.share.TimedChunk;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestTimedChunk {
    private static TimedChunk chunk;
    @BeforeClass
    public static void initTimedChunk()  {
     chunk = new TimedChunk("0001","0020",1,"ahoj",2001,3001);
    }

    @Test
    public void testChunkGetters(){
        assertEquals(3001,chunk.getDocumentId());
        assertEquals(2001,chunk.getId());
        assertEquals(1,chunk.getPartNumber());
        assertEquals("0001",chunk.getStartTime());
        assertEquals("0020",chunk.getEndTime());
        assertEquals("ahoj",chunk.getSurfaceForm());
    }
}
