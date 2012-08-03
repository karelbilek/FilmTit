package cz.filmtit.server;

import cz.filmtit.userspace.servlets.FilmTitBackendServer;
import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;

import java.io.File;

public class StartServer {

    public static FilmTitBackendServer back;
    public static FilmTitFrontendServer front;

    public static void main(String[] args) throws ClassNotFoundException {
       

        //dirty trick - initializing singleton
        ConfigurationSingleton.setConf(new Configuration(new File(args[0])));       
        
        int port = (args.length < 2) ? 80 : (Integer.parseInt(args[1]));
        front = new FilmTitFrontendServer(port);
    }

}
