package cz.filmtit.share;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 17.5.12
 * Time: 6:31
 * To change this template use File | Settings | File Templates.
 */


import org.junit.Test;

import java.lang.annotation.Annotation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestChunk {
    public void TestChunk()
    {
    }

    @Test
    public void TestChunkToString() {
        Chunk test = new Chunk("ahoj");
        assertEquals("ahoj",test.getSurfaceForm());
        assertEquals("Chunk[ahoj]",test.toString());

    }

    @Test
    public void TestChunkEquals()
    {
        Chunk test = new Chunk("ahoj");
        Chunk test2 = new Chunk("ahoj2");
        assertEquals(true, test.equals(test));
        assertEquals(false,test.equals(test2));
    }
}
