package cz.filmtit.server;

import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.core.Configuration;

import java.io.File;

public class StartServer {

    public static FilmTitBackendServer back;
    public static FilmTitFrontendServer front;

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        
        //start backend
        if (args.length == 0) {
            back = new FilmTitBackendServer();
        } else {
            back = new FilmTitBackendServer(new Configuration(new File(args[0])));
        };  

        //start frontend
        int port = (args.length < 2) ? 80 : (Integer.parseInt(args[1]));
        front = new FilmTitFrontendServer(port);
    }

}
