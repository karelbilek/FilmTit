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

package cz.filmtit.userspace.tests;


import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.core.io.data.FreebaseMediaSourceFactory;
import cz.filmtit.core.model.MediaSourceFactory;
import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.share.*;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidValueException;
import cz.filmtit.userspace.*;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;


/**
 * Test of the Session class functionality.
 *
 * @author Jindřich Libovický
 */
public class TestSession {
    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    @AfterClass
    public static void clean() {
        MockHibernateUtil.clearDatabase();
    }

    private LoremIpsum loremIpsum = new LoremIpsum();
    private TranslationMemory TM;
    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();
    private MediaSourceFactory mediaSourceFactory =
            new FreebaseMediaSourceFactory(ConfigurationSingleton.getConf().freebaseKey(), 10);


    public TestSession() {
        Configuration config = new Configuration(new File("configuration.xml"));
        TM = cz.filmtit.core.tests.TestUtil.createTMWithDummyContent(config);
                //Factory.createTMFromConfiguration(ConfigurationSingleton.conf(), true, false);
        mediaSourceFactory = new FreebaseMediaSourceFactory(config.freebaseKey(), 10);

    }

    @Test
    public void testDocumentResponse() throws NoSuchFieldException, IllegalAccessException {
        Session session = new Session(getSampleUser());

        DocumentResponse response = session.createNewDocument("Lost S01E01", "Lost", "en", mediaSourceFactory, "");

        assertNotNull(response.mediaSourceSuggestions);
        assertTrue(response.document.getId() != Long.MIN_VALUE);

        assertTrue(testIfDocumentInActiveList(session, response.document.getId()));
    }

    @Test
    public void testGetListOfDocuments() {
        Session session = new Session(getSampleUser());

        List<Document> expectedList = session.getListOfDocuments();
        assertEquals(3, expectedList.size());
        for (Document doc : expectedList) {
            assertTrue(doc.getTranslationResults() == null || doc.getTranslationResults().size() == 0);
        }
    }

    @Test
    public void testLoadDocument() throws InvalidDocumentIdException, NoSuchFieldException, IllegalAccessException {
        USUser sampleUser = getSampleUser();
        USDocument documentToRetrieve = firstGeneratedDocument;

        Session session = new Session(sampleUser);
        Document retrievedDocument = session.loadDocument(documentToRetrieve.getDatabaseId());

        // test if the documents are the same
        assertEquals(documentToRetrieve.getDatabaseId(), retrievedDocument.getId());

        // get the active documents field by reflection and test if the document was loaded to
        assertTrue(testIfDocumentInActiveList(session, documentToRetrieve.getDatabaseId()));
    }

    @Test(expected = InvalidDocumentIdException.class)
    public void testLoadNonexistingDocument() throws InvalidDocumentIdException {
        USUser sampleUser = getSampleUser();
        USDocument documentToRetrieve = firstGeneratedDocument;

        Session session = new Session(sampleUser);
        // TODO: should me done more elegantly
        Document retrievedDocument = session.loadDocument(10000006);
    }

    @Test
    public void testSetUserTranslation() throws InvalidChunkIdException, InvalidDocumentIdException {
        USUser sampleUser = getSampleUser();
        USTranslationResult trToUpdate = firstGeneratedTranslationResult;

        Session session = new Session(sampleUser);
        session.loadDocument(trToUpdate.getDocumentDatabaseId());
        session.setUserTranslation(trToUpdate.getTranslationResult().getSourceChunk().getChunkIndex(),
                trToUpdate.getDocumentDatabaseId(),
                "User translation", 0l);

        // test if the change appeared in the user space structure
        USTranslationResult changed = findTranslationResultInStructure(session, trToUpdate.getDocumentDatabaseId(),
                trToUpdate.getChunkIndex());
        assertEquals("User translation", changed.getUserTranslation());
        assertEquals(0l, changed.getSelectedTranslationPairID());

        // test if the change appeared in the database
        USTranslationResult fromDB = loadTranslationResultFromDb(changed.getDatabaseId());
        assertNotNull(fromDB);
        assertEquals("User translation", fromDB.getUserTranslation());
        assertEquals(0l, fromDB.getSelectedTranslationPairID());
    }

    @Test
    public void testChangeStartAndEndTime() throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        USUser sampleUser = getSampleUser();
        USTranslationResult trToUpdate = firstGeneratedTranslationResult;

        Session session = new Session(sampleUser);
        session.loadDocument(trToUpdate.getDocumentDatabaseId());

        String newStartTime = "00:00:54,377";
        String newEndTime = "00:01:00,373";

        session.setChunkStartTime(trToUpdate.getChunkIndex(), trToUpdate.getDocumentDatabaseId(), newStartTime);
        session.setChunkEndTime(trToUpdate.getChunkIndex(), trToUpdate.getDocumentDatabaseId(), newEndTime);

        // test if the change appeared in the user space structure
        USTranslationResult changed = findTranslationResultInStructure(session,
                trToUpdate.getDocumentDatabaseId(), trToUpdate.getChunkIndex());
        assertEquals(newStartTime, changed.getStartTime());
        assertEquals(newEndTime, changed.getEndTime());

        // test if the change appeared in the database
        USTranslationResult fromDB = loadTranslationResultFromDb(trToUpdate.getDatabaseId());
        assertNotNull(fromDB);
        assertEquals(newStartTime, fromDB.getStartTime());
        assertEquals(newEndTime, fromDB.getEndTime());
    }

    @Test
    public void testChangeOriginalText() throws InvalidDocumentIdException, InvalidChunkIdException {
        USUser sampleUser = getSampleUser();
        USTranslationResult trToUpdate = firstGeneratedTranslationResult;

        Session session = new Session(sampleUser);
        session.loadDocument(trToUpdate.getDocumentDatabaseId());

        List<TranslationPair> originalSuggestion = trToUpdate.getTranslationResult().getTmSuggestions();
        TranslationResult result =
                session.changeText(trToUpdate.getTranslationResult().getSourceChunk(), "New text", TM);

        assertNotNull(result);


        // test if the change appeared in the user space structure
        USTranslationResult changed = findTranslationResultInStructure(session,
                trToUpdate.getDocumentDatabaseId(), trToUpdate.getChunkIndex());
        assertEquals(trToUpdate.getDatabaseId(), changed.getDatabaseId());
        assertEquals("New text", changed.getText());

        // test if the change appeared in the database
        USTranslationResult fromDB = loadTranslationResultFromDb(trToUpdate.getDatabaseId());
        assertNotNull(fromDB);
        assertEquals("New text", fromDB.getText());
    }

    @Test
    public void testDeleteChunk() throws InvalidDocumentIdException, InvalidChunkIdException {
        USUser sampleUser = getSampleUser();
        USTranslationResult trToUpdate = firstGeneratedTranslationResult;

        Session session = new Session(sampleUser);
        session.loadDocument(trToUpdate.getDocumentDatabaseId());

        session.deleteChunk(trToUpdate.getChunkIndex(), trToUpdate.getDocumentDatabaseId());
    }

    @Test
    public void testTerminate() throws InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        USUser sampleUser = getSampleUser();

        Session session = new Session(sampleUser);

        DocumentResponse response = session.createNewDocument("Lost S01E01", "Lost", "en", mediaSourceFactory, "");
        Document clientDocument = response.document;
        if (response.mediaSourceSuggestions.size() > 0) {
            session.selectSource(clientDocument.getId(), response.mediaSourceSuggestions.get(0));
        }

        List<TimedChunk> timedChunks = new ArrayList<TimedChunk>();
        List<TranslationResult> clientTRList = new ArrayList<TranslationResult>();
        for (int i = 0; i < 9; ++i) {
            TimedChunk sampleTimedChunk = new TimedChunk("00:0" + i + ":00,000", "00:0" + (i + 1) + ":01,000", 0,
                    loremIpsum.getWords(5,5), i, clientDocument.getId());
            timedChunks.add(sampleTimedChunk);
        }
        session.saveSourceChunks(timedChunks);

        for (TimedChunk sampleTimedChunk : timedChunks) {
            TranslationResult serverRespond = session.getTranslationResults(sampleTimedChunk, TM);
            clientTRList.add(serverRespond);
        }

        for (TranslationResult tr : clientTRList) {
            session.setUserTranslation(tr.getSourceChunk().getChunkIndex(), tr.getDocumentId(), loremIpsum.getWords(5,5), 0);
        }

        session.logout();
    }

    @Test
    public void testSaveSourceChunks() throws InvalidDocumentIdException, InterruptedException, InvalidChunkIdException, InvalidValueException {
        Session session = new Session(getSampleUser());

        DocumentResponse resp = session.createNewDocument("Lost", "Lost", "en", mediaSourceFactory, "");
        long documentId = resp.document.getId();

        // generate few chunks
        List<TimedChunk> timedChunks = new ArrayList<TimedChunk>(32);
        for (int i = 0; i < 32; ++i) {
            timedChunks.add(new TimedChunk("00:00:00,000", "00:00:00,000", 0,
                    loremIpsum.getWords(5, i), i, documentId));
        }
        session.saveSourceChunks(timedChunks);

        Thread.sleep(2000l);

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List dbQuery =
            dbSession.createQuery("select t from USTranslationResult t where t.documentDatabaseId = " + documentId).list();
        usHibernateUtil.closeAndCommitSession(dbSession);

        assertEquals(32, dbQuery.size());
    }

    /**
     * Tests the standard deletion of a document. First a half of the translation results are marked as if they
     * have already sent feedback to the core. Then the delete method is called. It is tested that the deletion
     * sign was set and the deleted document does not appear in the list provided to the client. Then it is tested
     * if the chunks that already sent the feedback were deleted.
     */
    @Test
    public void testDeleteDocument()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InvalidDocumentIdException {
        Session session = new Session(getSampleUser());

        // originally there 3 documents
        List<Document> originallyDocuments = session.getListOfDocuments();
        assertEquals(3, originallyDocuments.size());

        session.loadDocument(firstGeneratedDocument.getDatabaseId());

        // each document of the sample user has 20 sample chunks ...
        //  ... simulate feedback has been sent from 10 of them
        USDocument document = firstGeneratedDocument;
        int feedbackedResults = 0;
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        for (USTranslationResult result : document.getTranslationResultValues()) {
            Method feedBackSentSetter = USTranslationResult.class.getDeclaredMethod("setFeedbackSent", boolean.class);
            feedBackSentSetter.setAccessible(true);
            feedBackSentSetter.invoke(result, true);
            result.saveToDatabase(dbSession);

            feedbackedResults++;
            if (feedbackedResults >= 10) { break; }
        }
        usHibernateUtil.closeAndCommitSession(dbSession);

        session.deleteDocument(firstGeneratedDocument.getDatabaseId());

        // test if it disappeared from the list of users documents
        List<Document> afterDeleteDocuments = session.getListOfDocuments();
        assertEquals(2, afterDeleteDocuments.size());
        assertTrue(firstGeneratedDocument.isToBeDeleted());

        dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List resultsFromDb = dbSession.createQuery("select r from USTranslationResult r where r.documentDatabaseId = :did")
                .setParameter("did", firstGeneratedDocument.getDatabaseId()).list();
        usHibernateUtil.closeAndCommitSession(dbSession);
        assertEquals(10, resultsFromDb.size());
    }

    /**
     * The first translation result generated in getSampleUser() method to be used in the test as
     * a sample translation result.
     */
    private USTranslationResult firstGeneratedTranslationResult = null;
    private USDocument firstGeneratedDocument = null;
    private int sampleUsersCount = 0;

    private USUser getSampleUser() {
        firstGeneratedTranslationResult = null;
        firstGeneratedDocument = null;

        USUser sampleUser = new USUser("Jindra the User no." + sampleUsersCount);
        LoremIpsum loremIpsum = new LoremIpsum();


        for (int i = 0; i < 3; ++i) {
            USDocument usDocument = new USDocument(new Document("Test", "en", ""), sampleUser, new ArrayList<DocumentUsers>());
            usDocument.setOwner(sampleUser);
            long documentID = usDocument.getDatabaseId();

            if (firstGeneratedDocument == null) {
                firstGeneratedDocument = usDocument;
            }

            for (int j = 0; j < 20; ++j) {
                USTranslationResult translationResult = new USTranslationResult(
                        new TimedChunk("01:43:29,000", "01:43:32,128", 0, loremIpsum.getWords(7, 10), j, documentID));

                translationResult.setDocument(usDocument);
                usDocument.addTranslationResult(translationResult);

                // keep the reference to the translation result
                if (firstGeneratedTranslationResult == null) {
                    firstGeneratedTranslationResult = translationResult;
                }
            }

            sampleUser.addDocument(usDocument);

            try {
                Method ownerDatabaseIDSetter = USDocument.class.getDeclaredMethod("setOwnerDatabaseId", long.class);
                ownerDatabaseIDSetter.setAccessible(true);
                ownerDatabaseIDSetter.invoke(usDocument, sampleUser.getDatabaseId());
            }
            catch (NoSuchMethodException e) { e.printStackTrace(); }
            catch (InvocationTargetException e) { e.printStackTrace(); }
            catch (IllegalAccessException e) { e.printStackTrace(); }

            org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
            usDocument.saveToDatabase(dbSession);
            usHibernateUtil.closeAndCommitSession(dbSession);
        }

        sampleUsersCount++;

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        sampleUser.saveToDatabase(dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);

        dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List dbRes = dbSession.createQuery("select u from USUser u where u.id = " + sampleUser.getDatabaseId()).list();
        if (dbRes.size() == 1) {
            //return (USUser)(dbRes.get(0));
            return sampleUser;
        }
        else {
            return null;
        }
    }

    private boolean testIfDocumentInActiveList(Session session, long id) throws IllegalAccessException, NoSuchFieldException {
        Field activeDocumentsField = Session.class.getDeclaredField("activeDocuments");
        activeDocumentsField.setAccessible(true);
        Map<Long, USDocument> activeDocuments = (Map<Long, USDocument>)(activeDocumentsField.get(session));

        return activeDocuments.containsKey(id);
    }

    /**
     * Loads USTranslationResult with given ID from the database.
     * @param id ID of requested USTranslationResult
     * @return The requested USTranslationResult
     */
    private USTranslationResult loadTranslationResultFromDb(long id) {
        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List dbRes = dbSession.createQuery("select r from USTranslationResult r where r.id = " + id).list();
        usHibernateUtil.closeAndCommitSession(dbSession);
        if (dbRes.size() == 1) {
            return (USTranslationResult)(dbRes.get(0));
        }
        else {
            return null;
        }
    }

    /**
     * Finds the chunk in given document with such index.
     * @param session Session within the result is supposed to be found
     * @param index Index of the source chunk
     * @return Requested result
     */
    private USTranslationResult findTranslationResultInStructure(Session session, long documentId, ChunkIndex index) {
        USTranslationResult changed = null;
        for (USDocument document : session.getUser().getOwnedDocuments().values()) {
            if (document.getDatabaseId() == documentId) {
                for (USTranslationResult result : document.getTranslationResultValues()) {
                    if (result.getChunkIndex().equals(index)) {
                        changed = result;
                    }
                }
            }
        }
        return changed;
    }

    @Test
    public void testUserSettings() throws InvalidValueException, InvalidChunkIdException {
        USUser user = getSampleUser();
        Session session = new Session(user);

        // test initial settings
        assertEquals(ConfigurationSingleton.conf().maximumSuggestionsCount(), user.getMaximumNumberOfSuggestions());
        assertEquals(true, user.getUseMoses());
        assertEquals(false, user.isPermanentlyLoggedId());

        session.setEmail("huhu@huhu.hu");
        session.setMaximumNumberOfSuggestions(23);
        session.setPermanentlyLoggedIn(true);
        session.setUseMoses(false);

        // test changed settings
        assertEquals("huhu@huhu.hu", user.getEmail());
        assertEquals(23, user.getMaximumNumberOfSuggestions());
        assertEquals(false, user.getUseMoses());
        assertEquals(true, user.isPermanentlyLoggedId());
    }

    //changeDocumentTitle, changeMovieTitle

    @Test
    public void testChangeDocumentTitle() throws InvalidDocumentIdException {
        USUser user = getSampleUser();
        Session session = new Session(user);

        USDocument document = firstGeneratedDocument;

        session.changeDocumentTitle(document.getDatabaseId(), "Changed title");

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List fromDb = dbSession.createQuery("select d from USDocument d where d.databaseId = " + document.getDatabaseId()).list();
        usHibernateUtil.closeAndCommitSession(dbSession);

        assertEquals(1, fromDb.size());
        assertEquals("Changed title", ((USDocument)fromDb.get(0)).getTitle());
    }

    public void testChangeMovieTitle() throws InvalidDocumentIdException {
        USUser user = getSampleUser();
        Session session = new Session(user);

        USDocument document = firstGeneratedDocument;

        session.changeMovieTitle(document.getDatabaseId(), "Movie", mediaSourceFactory);
        assertEquals(null, document.getMediaSource());

        org.hibernate.Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        List fromDb = dbSession.createQuery("select d from USDocument d where d.databaseId = " + document.getDatabaseId()).list();
        usHibernateUtil.closeAndCommitSession(dbSession);

        assertEquals(1, fromDb.size());
        assertEquals(null, ((USDocument)(fromDb.get(0))).getMediaSource());
    }

}
