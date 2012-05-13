package cz.filmtit.server;

import java.net.URL;
import java.security.ProtectionDomain;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import java.io.File;

//Class mainly copied from web
public class FilmTitFrontendServer {
 
  public FilmTitFrontendServer(int port) {

    //using long version so it doesn't conflict with something
    //else potentially named Server
    org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server();
    SocketConnector connector = new SocketConnector();
 
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60);
    connector.setSoLingerTime(-1);
    connector.setPort(port);
    server.setConnectors(new Connector[] { connector });
 
    WebAppContext context = new WebAppContext();
    context.setServer(server);

    context.setContextPath("/");

 
    //a little hack
    ProtectionDomain protectionDomain = FilmTitFrontendServer.class.getProtectionDomain();
    URL location = protectionDomain.getCodeSource().getLocation();

    context.setWar(location.toExternalForm());
    context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");

    context.setWar(location.toExternalForm());
 
    server.setHandler(context);
    try {
      server.start();
      System.in.read();
      server.stop();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }
}
