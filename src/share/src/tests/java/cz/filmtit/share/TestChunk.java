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
        private Chunk test = new Chunk("ahoj");
    public void TestChunk()
    {


    }
    @Test
    public void TestChunkToString() {
         Chunk test = new Chunk("ahoj");
        assertEquals("Chunk[ahoj]",test.toString());

    }
    @Test
    public void TestChunkToString() {
        Chunk test = new Chunk("ahoj");
        assertEquals("Chunk[ahoj]",test.toString());

    }
}
