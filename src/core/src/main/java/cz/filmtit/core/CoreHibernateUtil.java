package cz.filmtit.core;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class CoreHibernateUtil {
    private static SessionFactory sessionFactory = null;
    private static ServiceRegistry serviceRegistry;

    public static java.net.URL configurationFile = CoreHibernateUtil.class.getResource("/cz/filmtit/core/core.cfg.xml");

    /**
     * A path to the Hibernate configuration file. If it's necessary to change it (e.g. for unit testing),
     * it has to be done using reflection before the getSessionFactory method is called for the first time.
     */
    private static SessionFactory buildSessionFactory() {
        try {
            cz.filmtit.core.Configuration projectConfiguration = ConfigurationSingleton.conf();

            // Create the SessionFactory from core.cfg.xml
            Configuration hibernateConfiguration = new Configuration();

            hibernateConfiguration.configure(configurationFile);

            hibernateConfiguration.setProperty("hibernate.connection.username", projectConfiguration.dbUser());
            hibernateConfiguration.setProperty("hibernate.connection.password", projectConfiguration.dbPassword());
            hibernateConfiguration.setProperty("hibernate.connection.url", projectConfiguration.dbConnector());

            serviceRegistry = new ServiceRegistryBuilder().applySettings(hibernateConfiguration.getProperties()).buildServiceRegistry();
            sessionFactory = hibernateConfiguration.buildSessionFactory(serviceRegistry);
            return sessionFactory;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void buildSessionFactoryFromHbmFile(String hbmFileName) {
        Configuration configuration = new Configuration();
        configuration.configure(hbmFileName);

        serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public static org.hibernate.Session getSessionWithActiveTransaction() {
        org.hibernate.Session dbSession = CoreHibernateUtil.getSessionFactory().openSession();
        dbSession.beginTransaction();
        return dbSession;
    }

    public static void closeAndCommitSession(org.hibernate.Session dbSession){
        dbSession.getTransaction().commit();
        dbSession.close();
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }

        return sessionFactory;
    }
}
