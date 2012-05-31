package cz.filmtit.share;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 17.5.12
 * Time: 6:31
 * To change this template use File | Settings | File Templates.
 */


import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TestMediaSource {
    private static MediaSource source;
    @BeforeClass
    public static void initTimedChunk(){
     source = new MediaSource("TestMovie","2012","akční, komedie, thriller");
    }



    @Test
    public void testChunkGetters(){
        assertEquals("TestMovie",source.getTitle());
        assertEquals("2012",source.getYear());
        assertEquals(3,source.getGenres().size());
        assertEquals(true,source.getGenres().contains("komedie"));
    }
}
