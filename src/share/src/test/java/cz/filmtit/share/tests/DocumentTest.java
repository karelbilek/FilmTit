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
        doc = new Document("TestMovie", "cs");
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
