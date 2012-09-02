package cz.filmtit.server;

import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;

import java.io.File;

/**
 * Helper class for starting the FilmTit service with the specified configuration file on the
 * specified port.
 */

public class StartServer {

    public static FilmTitBackendServer back;
    public static FilmTitFrontendServer front;

    public static void main(String[] args) throws ClassNotFoundException {

        //We need to initialize a Singleton containing the configuration to be able to
        //pass it to all classes.
        ConfigurationSingleton.setConf(new Configuration(new File(args[0])));       

        //If no port is specified, try port 80
        int port = (args.length < 2) ? 80 : (Integer.parseInt(args[1]));

        //Create the front-end server
        front = new FilmTitFrontendServer(port);
    }

}
