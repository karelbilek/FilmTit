package cz.filmtit.userspace.tests;

import cz.filmtit.share.Document;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.userspace.Session;
import cz.filmtit.userspace.USDocument;
import cz.filmtit.userspace.USTranslationResult;
import cz.filmtit.userspace.USUser;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


/**
 * Test of the Session class functionality.
 *
 * @author Jindřich Libovický
 */
public class TestSession {
    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
    }

    @Test
    public void testDocumentResponse() {
        Session session = new Session(getSampleUser());

        //public DocumentResponse createNewDocument(String movieTitle, String year, String language, TranslationMemory TM)
    }



    // methods to be tested
    // terminate()

    // public TranslationResult getTranslationResults(TimedChunk chunk, TranslationMemory TM) throws InvalidDocumentIdException

    //public Void setUserTranslation(int chunkId, long documentId, String userTranslation, long chosenTranslationPairID)

    @Test
    public void testGetListOfDocuments() {
        Session session = new Session(getSampleUser());

        List<Document> expectedList = session.getListOfDocuments();
        assertEquals(3, expectedList.size());
    }

    @Test
    public void testLoadDocument() throws InvalidDocumentIdException, NoSuchFieldException, IllegalAccessException {
        USUser sampleUser = getSampleUser();
        USDocument documentToRetrieve = sampleUser.getOwnedDocuments().get(0);

        Session session = new Session(sampleUser);
        Document retrievedDocument = session.loadDocument(documentToRetrieve.getDatabaseId());

        // test if the documents are the same
        assertEquals(documentToRetrieve.getDatabaseId(), retrievedDocument.getId());

        // get the active documents field by reflection and test if the document was loaded to
        testIfDocumentInActiveList(session, documentToRetrieve.getDatabaseId());
    }

    @Test(expected = InvalidDocumentIdException.class)
    public void testLoadNonexistingDocument() throws InvalidDocumentIdException {
        throw new InvalidDocumentIdException("");
    }

    private USUser getSampleUser() {
        USUser sampleUser = new USUser("Jindra the User");
        LoremIpsum loremIpsum = new LoremIpsum();


        for (int i = 0; i < 3; ++i) {
            USDocument usDocument = new USDocument(new Document("Movie " + i, "2012", "en"));
            long documentID = usDocument.getDatabaseId();

            for (int j = 0; j < 20; ++j) {
                USTranslationResult translationResult = new USTranslationResult(
                        new TimedChunk("01:43:29,000", "01:43:32,128", 0, loremIpsum.getWords(7, 10), 1, documentID));

                translationResult.setParent(usDocument);
                usDocument.addTranslationResult(translationResult);
            }

            sampleUser.addDocument(usDocument);
        }

        return sampleUser;
    }

    private void testIfDocumentInActiveList(Session session, long id) throws IllegalAccessException, NoSuchFieldException {
        Field activeDocumentsField = Session.class.getDeclaredField("activeDocuments");
        activeDocumentsField.setAccessible(true);
        Map<Long, USDocument> activeDocuments = (Map<Long, USDocument>)(activeDocumentsField.get(session));

        assertTrue(activeDocuments.containsKey(id));

    }
}
