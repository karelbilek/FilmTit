package cz.filmtit.userspace;

import java.net.URL;
import java.security.ProtectionDomain;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import java.io.File;

public class Start {
 
  public static void main(String[] args) throws Exception {
    Server server = new Server();
    SocketConnector connector = new SocketConnector();
 
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60);
    connector.setSoLingerTime(-1);
    connector.setPort(8880);
    server.setConnectors(new Connector[] { connector });
 
    WebAppContext context = new WebAppContext();
    context.setServer(server);

    context.setContextPath("/");

 
    ProtectionDomain protectionDomain = Start.class.getProtectionDomain();

    //this will NOT work on the server! (of course)
    URL location = new File("/afs/ms.mff.cuni.cz/u/b/bilek7am/filmtit/FilmTit/src/gui/target/gui-0.1").toURL();
    
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
