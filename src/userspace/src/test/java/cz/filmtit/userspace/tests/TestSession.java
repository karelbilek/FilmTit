package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.Document;
import cz.filmtit.share.DocumentResponse;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.userspace.Session;
import cz.filmtit.userspace.USDocument;
import cz.filmtit.userspace.USTranslationResult;
import cz.filmtit.userspace.USUser;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
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

    LoremIpsum loremIpsum = new LoremIpsum();

    @Test
    public void testDocumentResponse() throws NoSuchFieldException, IllegalAccessException {
        Configuration config = new Configuration(new File("configuration.xml"));
        TranslationMemory TM = cz.filmtit.core.tests.TestUtil.createTMWithDummyContent(config);

        Session session = new Session(getSampleUser());

        DocumentResponse response = session.createNewDocument("Movie title", "2012", "en", TM);

        assertTrue(response.mediaSourceSuggestions.size() > 0);
        assertEquals("Movie title", response.document.getMovie().getTitle());
        assertEquals("2012", response.document.getMovie().getYear());
        assertTrue(response.document.getId() != Long.MIN_VALUE);

        testIfDocumentInActiveList(session, response.document.getId());
    }

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
        USUser sampleUser = getSampleUser();
        USDocument documentToRetrieve = sampleUser.getOwnedDocuments().get(0);

        Session session = new Session(sampleUser);
        // TODO: should me done more elegantly
        Document retrievedDocument = session.loadDocument(10000006);
    }

    @Test
    public void testGetTranslationResults() throws InvalidDocumentIdException, NoSuchFieldException, IllegalAccessException {
        Configuration config = new Configuration(new File("configuration.xml"));
        TranslationMemory TM = cz.filmtit.core.tests.TestUtil.createTMWithDummyContent(config);

        USUser sampleUser = getSampleUser();
        long documentID = sampleUser.getOwnedDocuments().get(0).getDatabaseId();

        Session session = new Session(sampleUser);
        session.loadDocument(documentID);

        TimedChunk sampleTimedChunk = new TimedChunk("00:00:00,000", "00:00:01,000", 0,
                loremIpsum.getWords(5,5), 150, documentID);

        session.getTranslationResults(sampleTimedChunk, TM);

        // test if the translation result ended up in the table of active ones
        Field activeResultsField = Session.class.getDeclaredField("activeTranslationResults");
        activeResultsField.setAccessible(true);
        Map<Long, Map<Integer, USTranslationResult>> activeTranslationResults =
                (Map<Long, Map<Integer, USTranslationResult>>)(activeResultsField.get(session));

        assertTrue(activeTranslationResults.containsKey(documentID));
        assertTrue(activeTranslationResults.get(documentID).containsKey(150));
    }

    @Test
    public void testSetUserTranslation() throws InvalidChunkIdException, InvalidDocumentIdException {
        USUser sampleUser = getSampleUser();
        USTranslationResult trToUpdate = sampleUser.getOwnedDocuments().get(0).getTranslationsResults().get(0);

        Session session = new Session(sampleUser);
        session.loadDocument(trToUpdate.getDocumentDatabaseId());
        session.setUserTranslation(trToUpdate.getSharedId(), trToUpdate.getDocumentDatabaseId(),
                "User translation", 0l);

        USTranslationResult changed = null;
        for (USTranslationResult result : session.getUser().getOwnedDocuments().get(0).getTranslationsResults()) {
            if (result.getSharedId() == trToUpdate.getSharedId()) {
                changed = result;
            }
        }

        assertEquals("User translation", changed.getUserTranslation());
        assertEquals(0l, changed.getSelectedTranslationPairID());
    }

    @Test
    public void testTerminate() {
        USUser sampleUser = getSampleUser();

        Session session = new Session(sampleUser);

        // do a scenario 1. add document
        //               2. add translation result to the document
        //               3. set the user translation


        session.kill();

        // test if everything was properly saved to database
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
