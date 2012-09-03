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

package cz.filmtit.server;

import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.userspace.servlets.SubtitleDownloadServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;

/**
 * A front-end web server implementation for running the FilmTit service.
 */
public class FilmTitFrontendServer {

    public FilmTitFrontendServer(int port) {

        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);
        URL location;

        //This is required for running the Server from either Maven/the class directly or from the jar
        if (FilmTitFrontendServer.class.getResource("FilmTitFrontendServer.class").toString().startsWith("jar:")) {

            //running from shaded jar
            ProtectionDomain protectionDomain = FilmTitFrontendServer.class.getProtectionDomain();
            location = protectionDomain.getCodeSource().getLocation();
        } else {

            //running from class
            try {
                location = new File("gui/target/gui-0.1").toURL();
            } catch (MalformedURLException e) {
                System.err.println("File does not exist.");
                return;
                //System.exit(0);
            }
        }


        //======first servlet - .html, .css
        WebAppContext frontContext = new WebAppContext();
        frontContext.setServer(server);
        frontContext.setContextPath("/");
        frontContext.setDescriptor(location.toExternalForm() + "WEB-INF/web.xml");
        frontContext.setWar(location.toExternalForm());


        //=====second servlet - RPC server
        //I call it backend, but it still has the "gui" URL
        final ServletContextHandler backContext = new ServletContextHandler(server, "/gui", ServletContextHandler.SESSIONS);

        //I use trick with config singleton to get config loaded there
        FilmTitBackendServer backend = new FilmTitBackendServer();
        backContext.addServlet(new ServletHolder(backend), "/filmtit");


        //======third servlet - for subtitles
        final ServletContextHandler subDownloadContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        subDownloadContext.setContextPath("/download");
        subDownloadContext.addServlet(new ServletHolder(new SubtitleDownloadServlet(backend)), "/download");


        //======setting up everything
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { backContext, frontContext, subDownloadContext});
        server.setHandler(contexts);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}
