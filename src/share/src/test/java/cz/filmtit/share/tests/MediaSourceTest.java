/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.share.tests;

/**
 * Created with IntelliJ IDEA.
 * User: josef.cech
 * Date: 17.5.12
 * Time: 6:31
 * To change this template use File | Settings | File Templates.
 */


import cz.filmtit.share.MediaSource;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MediaSourceTest {
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
