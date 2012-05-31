package cz.filmtit.share.tests;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 17.5.12
 * Time: 6:31
 * To change this template use File | Settings | File Templates.
 */


import cz.filmtit.share.Chunk;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestChunk {
    public void TestChunk()
    {
    }

    @Test
    public void testChunkToString() {
        Chunk test = new Chunk("ahoj");
        assertEquals("ahoj",test.getSurfaceForm());
        assertEquals("Chunk[ahoj]",test.toString());

    }

    @Test
    public void testChunkEquals()
    {
        Chunk test = new Chunk("ahoj");
        Chunk test2 = new Chunk("ahoj2");
        assertEquals(true, test.equals(test));
        assertEquals(false,test.equals(test2));
    }
}
