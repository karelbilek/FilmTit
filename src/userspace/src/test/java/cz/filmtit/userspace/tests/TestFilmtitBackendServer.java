package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.share.DocumentResponse;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import cz.filmtit.userspace.IdGenerator;
import cz.filmtit.userspace.Session;
import cz.filmtit.userspace.USUser;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.MessageException;

import java.lang.reflect.Field;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jindřich Libovický
 */
public class TestFilmtitBackendServer {
    private LoremIpsum loremIpsum = new LoremIpsum();

    @BeforeClass
    public static void setupConfiguration() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    @Test
    public void testGetAutheticationURL() throws ConsumerException, MessageException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        //server.authenticateOpenId("http://google.com/");
    }

    @Test(expected = InvalidSessionIdException.class)
    public void testSessionTimeOut() throws NoSuchFieldException, IllegalAccessException, InterruptedException, InvalidSessionIdException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        Session session = new Session(new USUser("jindra", "pinda", "jindra@pinda.cz", null));

        // change the last operation time in session
        Field lastChangeField = null;
        lastChangeField = Session.class.getDeclaredField("lastOperationTime");
        lastChangeField.setAccessible(true);
        lastChangeField.set(session, new Date().getTime() -  2 * ConfigurationSingleton.getConf().sessionTimeout());

        String sessionId = placeSessionToTheServer(server, session);

        Thread.sleep(60 * 1000l + 5000);

        server.getListOfDocuments(sessionId);
    }

    @Test
    public void testParallelGetTranslationResults()
            throws NoSuchFieldException, IllegalAccessException, InvalidSessionIdException, InvalidDocumentIdException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        Session session = new Session(new USUser("jindra", "pinda", "jindra@pinda.cz", null));
        String sessionId = placeSessionToTheServer(server, session);

        DocumentResponse resp = server.createNewDocument(sessionId, "Lost", "Lost", "en");
        long documentId = resp.document.getId();

        // generate few chunks
        List<TimedChunk> timedChunks = generateTimedChunks(documentId);

        List<TranslationResult> res = server.getTranslationResults(sessionId, timedChunks);
        assertEquals(timedChunks.size(), res.size());
    }

    @Test(expected = Throwable.class)
    public void testCloseDocumentWhileGettingSuggestions()
            throws NoSuchFieldException, IllegalAccessException, InvalidSessionIdException, InvalidDocumentIdException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        Session session = new Session(new USUser("jindra", "pinda", "jindra@pinda.cz", null));
        String sessionId = placeSessionToTheServer(server, session);

        DocumentResponse resp = server.createNewDocument(sessionId, "Lost", "Lost", "en");
        long documentId = resp.document.getId();
        List<TimedChunk> chunks = generateTimedChunks(documentId);

        server.closeDocument(sessionId, documentId);
        GettingTranslationsRunner runner = new GettingTranslationsRunner(server, sessionId, chunks);
        runner.run();

    }

    private class GettingTranslationsRunner extends Thread {
        private FilmTitBackendServer server;
        private String sessionId;
        private List<TimedChunk> chunks;

        public GettingTranslationsRunner(FilmTitBackendServer server, String sessionId, List<TimedChunk> chunks) {
            this.server = server;
            this.sessionId = sessionId;
            this.chunks = chunks;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(500);
                server.getTranslationResults(sessionId, chunks);
            } catch (InvalidSessionIdException e) {
                e.printStackTrace();
            } catch (InvalidDocumentIdException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String placeSessionToTheServer(FilmTitBackendServer server, Session session)
            throws NoSuchFieldException, IllegalAccessException {
        String sessionId = new IdGenerator().generateId(47);

        Map<String, Session> activeSessions = Collections.synchronizedMap(new HashMap<String, Session>());
        activeSessions.put(sessionId, session);

        // add the session to active sessions
        Field activeSessionsField = FilmTitBackendServer.class.getDeclaredField("activeSessions");
        activeSessionsField.setAccessible(true);
        activeSessionsField.set(server, activeSessions);

        return sessionId;
    }

    private List<TimedChunk> generateTimedChunks(long documentId) {
        List<TimedChunk> timedChunks = new ArrayList<TimedChunk>(32);
        for (int i = 0; i < 32; ++i) {
            timedChunks.add(new TimedChunk("00:00:00.000", "00:00:00.000", 0,
                    loremIpsum.getWords(5, i), i, documentId));
        }
        return timedChunks;
    }
}
