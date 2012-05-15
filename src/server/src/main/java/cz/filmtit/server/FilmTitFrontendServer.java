package cz.filmtit.server;

import cz.filmtit.core.Configuration;
import java.net.URL;
import cz.filmtit.userspace.FilmTitBackendServer;
import java.security.ProtectionDomain;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import java.io.File;

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

//a little hack
    ProtectionDomain protectionDomain = FilmTitFrontendServer.class.getProtectionDomain();
    URL location = protectionDomain.getCodeSource().getLocation(); 

    WebAppContext front_context = new WebAppContext();
    front_context.setServer(server);
    front_context.setContextPath("/");
    front_context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
    
    //It is still setWar, but it's OK, the classes are not in the WEB-INF so they are not run
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
