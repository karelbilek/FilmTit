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


import cz.filmtit.share.TimedChunk;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TimedChunkTest {
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
