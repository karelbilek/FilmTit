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
import cz.filmtit.share.DocumentResponse;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import cz.filmtit.share.exceptions.InvalidChunkIdException;
import cz.filmtit.share.exceptions.InvalidDocumentIdException;
import cz.filmtit.share.exceptions.InvalidSessionIdException;
import cz.filmtit.share.ChunkStringGenerator;
import cz.filmtit.share.exceptions.InvalidValueException;
import cz.filmtit.userspace.IdGenerator;
import cz.filmtit.userspace.Session;
import cz.filmtit.userspace.USUser;
import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.MessageException;

import java.lang.reflect.Field;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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

    @AfterClass
    public static void clean() {
        MockHibernateUtil.clearDatabase();
    }

    @Test
    public void testGetAutheticationURL() throws ConsumerException, MessageException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        //server.authenticateOpenId("http://google.com/");
    }

    @Test(expected = InvalidSessionIdException.class)
    public void testSessionTimeOut() throws NoSuchFieldException, IllegalAccessException, InterruptedException, InvalidSessionIdException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        Session session = new Session(new USUser("jindrapinda", "pinda", "jindra@pinda.cz", null));

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
            throws NoSuchFieldException, IllegalAccessException, InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        Session session = new Session(new USUser("jindra", "pinda", "jindra@pinda.cz", null));
        String sessionId = placeSessionToTheServer(server, session);

        DocumentResponse resp = server.createNewDocument(sessionId, "Lost", "Lost", "en", "");
        long documentId = resp.document.getId();

        // generate few chunks
        List<TimedChunk> timedChunks = generateTimedChunks(documentId);

        server.saveSourceChunks(sessionId, timedChunks);
        List<TranslationResult> res = server.getTranslationResults(sessionId, timedChunks);
        assertEquals(timedChunks.size(), res.size());
    }

    @Test
    public void testGetSourceSubtitlesExport()
            throws NoSuchFieldException, IllegalAccessException, InvalidSessionIdException, InvalidDocumentIdException, InvalidChunkIdException, InvalidValueException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();
        Session session = new Session(new USUser("jindra2", "pinda", "jindra@pinda.cz", null));
        String sessionId = placeSessionToTheServer(server, session);

        DocumentResponse resp = server.createNewDocument(sessionId, "Hannah and her sisters", "Hannah and her sisters", "en", "");
        long documentId = resp.document.getId();

        List<TimedChunk> chunks = new ArrayList<TimedChunk>();
        chunks.add(new TimedChunk("00:02:20,859", "00:02:24,362", 0, "I dream about her.", 1, documentId));
        chunks.add(new TimedChunk("00:02:25,405", "00:02:28,283", 0, "Oh, Lee. What am l gonna do?", 2, documentId));
        chunks.add(new TimedChunk("00:02:29,409", "00:02:33,330", 0, "I hear myself mooning over you,", 3, documentId));
        chunks.add(new TimedChunk("00:02:29,409", "00:02:33,330", 1, "and it's disgusting.", 4, documentId));

        server.saveSourceChunks(sessionId, chunks);

        String expectedSrtFile = "1\n" +
                "00:02:20,859 --> 00:02:24,362\n" +
                "I dream about her.\n" +
                "\n" +
                "2\n" +
                "00:02:25,405 --> 00:02:28,283\n" +
                "Oh, Lee. What am l gonna do?\n" +
                "\n" +
                "3\n" +
                "00:02:29,409 --> 00:02:33,330\n" +
                "I hear myself mooning over you,\n" +
                "and it's disgusting.\n\n";
        String actualSrtFile = server.getSourceSubtitles(sessionId, documentId, 25d, TimedChunk.FileType.SRT, ChunkStringGenerator.SOURCE_SIDE);
        assertEquals(expectedSrtFile, actualSrtFile);

        String expectedSubFile = "{3521}{3609}{I dream about her.}\n" +
                "{3635}{3707}{Oh, Lee. What am l gonna do?}\n" +
                "{3735}{3833}{I hear myself mooning over you,|and it's disgusting.}\n";
        String actualSubFile = server.getSourceSubtitles(sessionId, documentId, 25d, TimedChunk.FileType.SUB, ChunkStringGenerator.SOURCE_SIDE);
        assertEquals(expectedSubFile, actualSubFile);

        String expectedTxtFile = "I dream about her.\n" +
                "Oh, Lee. What am l gonna do?\n" +
                "I hear myself mooning over you, and it's disgusting.\n";
        String actualTxtFile = server.getSourceSubtitles(sessionId, documentId, 25d, TimedChunk.FileType.TXT, ChunkStringGenerator.SOURCE_SIDE);
        assertEquals(expectedTxtFile, actualTxtFile);
    }

    @Test
    public void testGetTargetSubtitlesExport() {
        // TODO: implement it
    }

    @Test
    public void testCancelingOldSessionOnRelogin() throws NoSuchFieldException, IllegalAccessException, InvalidValueException {
        FilmTitBackendServer server = new MockFilmTitBackendServer();

        // now get the table of active sessions
        Field activeSessionsField = FilmTitBackendServer.class.getDeclaredField("activeSessions");
        activeSessionsField.setAccessible(true);
        Map<String, Session> activeSessions = (Map<String, Session>) activeSessionsField.get(server);

        server.registration("user", "pass", "user@user.bf", null);
        String sessionId1 = server.simpleLogin("user", "pass").sessionID;

        assertTrue(activeSessions.size() == 1);
        assertTrue(activeSessions.containsKey(sessionId1));

        String sessionId2 = server.simpleLogin("user", "pass").sessionID;
        assertTrue(activeSessions.size() == 1);
        assertFalse(activeSessions.containsKey(sessionId1));
        assertTrue(activeSessions.containsKey(sessionId2));
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

        Map<Long, String> usersSessionIds = Collections.synchronizedMap(new HashMap<Long, String>());
        usersSessionIds.put(session.getUserDatabaseId(), sessionId);

        Field usersSessionIdsField = FilmTitBackendServer.class.getDeclaredField("usersSessionIds");
        usersSessionIdsField.setAccessible(true);
        usersSessionIdsField.set(server, usersSessionIds);


        return sessionId;
    }

    private List<TimedChunk> generateTimedChunks(long documentId) {
        List<TimedChunk> timedChunks = new ArrayList<TimedChunk>(32);
        for (int i = 0; i < 32; ++i) {
            timedChunks.add(new TimedChunk("00:00:00,000", "00:00:00,000", 0,
                    loremIpsum.getWords(5, 1), i, documentId));
        }
        return timedChunks;
    }
}
