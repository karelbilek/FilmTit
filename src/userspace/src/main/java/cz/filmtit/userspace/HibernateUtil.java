package cz.filmtit.userspace;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.io.File;

public class HibernateUtil {
    private static SessionFactory sessionFactory = null;
    private static ServiceRegistry serviceRegistry;

    /**
     * A path to the Hibernate configuration file. If it's necessary to change it (e.g. for unit testing),
     * it has to be done using reflection before the getSessionFactory method is called for the first time.
     */
    private static String configurationFile = "cz/filmtit/userspace/hibernate.cfg.xml";

    private static SessionFactory buildSessionFactory() {
        try {
            cz.filmtit.core.Configuration projectConfiguration = new cz.filmtit.core.Configuration(new File("/filmtit/git/FilmTit/src/configuration.xml"));
            //projectConfiguration.

            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure(configurationFile);

            configuration.setProperty("hibernate.connection.username", projectConfiguration.dbUser());
            configuration.setProperty("hibernate.connection.password", projectConfiguration.dbPassword());
            configuration.setProperty("hibernate.connection.url", projectConfiguration.dbConnector());

            serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            return sessionFactory;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void buildSessionFactoryFromHbmFile(String hmbFileName) {
        Configuration configuration = new Configuration();
        configuration.configure(configurationFile);

        serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }
}
