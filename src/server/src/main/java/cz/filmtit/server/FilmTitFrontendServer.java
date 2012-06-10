package cz.filmtit.server;

import cz.filmtit.core.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import cz.filmtit.userspace.FilmTitBackendServer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;

//Class mainly copied from web
public class FilmTitFrontendServer {
 
  public FilmTitFrontendServer(int port) {

    org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server();
    SocketConnector connector = new SocketConnector();
 
    //This I copied from web, not sure what it does
    connector.setMaxIdleTime(1000 * 60 * 60);
    connector.setSoLingerTime(-1);
    connector.setPort(port);
    server.setConnectors(new Connector[] { connector });


//a little hack, this will have to be deleted in actual code
    if (FilmTitFrontendServer.class.getResource("FilmTitFrontendServer.class").startsWith("jar:") {

        //running from shaded jar
        ProtectionDomain protectionDomain = FilmTitFrontendServer.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
    else {

        //running from class
        try {
            location = new File("gui/target/gui-0.1").toURL();
        } catch (MalformedURLException e) {
            System.err.println("File does not exist.");
            System.exit(0);
        }
    }

      WebAppContext front_context = new WebAppContext();
    front_context.setServer(server);
    front_context.setContextPath("/");
    front_context.setDescriptor(location.toExternalForm() + "WEB-INF/web.xml");


    

    front_context.setWar(location.toExternalForm());


    //I call it backend, but it still has the "gui" URL
    final ServletContextHandler back_context = new ServletContextHandler(server, "/gui", ServletContextHandler.SESSIONS);
    
                                                //I use the trick with singleton
    back_context.addServlet(new ServletHolder(new FilmTitBackendServer()), "/filmtit");

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] { back_context, front_context });
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
