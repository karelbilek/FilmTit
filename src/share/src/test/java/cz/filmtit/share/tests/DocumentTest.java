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


import cz.filmtit.share.Document;
import cz.filmtit.share.Language;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DocumentTest {
    private Document doc = null;

    private  void initializeDoc() {
        doc = new Document("TestMovie", "cs", "");
        doc.setId(2012);
    }

    public DocumentTest()
    {
        this.initializeDoc() ;
    }
    @Test
    public void testSetId() {

        if (doc == null) {initializeDoc();}  ;
        try{
          doc.setId(2013);
        }
        catch (Exception e) {
           assertEquals("Once the document ID is set, it cannot be changed.",e.getMessage());
        };


    }

    @Test
    public void testGetters() {
        if (doc == null) {initializeDoc();}  ;
       assertEquals("TestMovie",doc.getTitle());
       assertEquals(Language.fromCode("cs"),doc.getLanguage());

    }


}
