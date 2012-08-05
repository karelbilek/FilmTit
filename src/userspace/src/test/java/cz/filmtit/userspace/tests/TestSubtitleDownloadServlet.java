package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.userspace.servlets.SubtitleDownloadServlet;
import org.junit.Test;

/**
 * @author Jindřich Libovický
 */
public class TestSubtitleDownloadServlet {
    @Test
    public void testGettingProperFile() {
        FilmTitBackendServer backendServer = new MockFilmTitBackendServer();
        SubtitleDownloadServlet downloadServlet = new SubtitleDownloadServlet(backendServer);

        //HttpServletRequest
    }
}
