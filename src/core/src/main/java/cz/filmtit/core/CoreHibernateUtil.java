package cz.filmtit.core;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class CoreHibernateUtil {
    private SessionFactory sessionFactory = null;
    private ServiceRegistry serviceRegistry;

    public java.net.URL getConfigurationFile() {
        return CoreHibernateUtil.class.getResource("/cz/filmtit/core/core.cfg.xml");
    }

    /**
     * A path to the Hibernate configuration file. If it's necessary to change it (e.g. for unit testing),
     * it has to be done using reflection before the getSessionFactory method is called for the first time.
     */
    private SessionFactory buildSessionFactory() {
        try {
            cz.filmtit.core.Configuration projectConfiguration = ConfigurationSingleton.conf();

            // Create the SessionFactory from core.cfg.xml
            Configuration hibernateConfiguration = new Configuration();

            hibernateConfiguration.configure(getConfigurationFile());

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

    public  void buildSessionFactoryFromHbmFile(String hbmFileName) {
        Configuration configuration = new Configuration();
        configuration.configure(hbmFileName);

        serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public  org.hibernate.Session getSessionWithActiveTransaction() {
        org.hibernate.Session dbSession = getSessionFactory().openSession();
        dbSession.beginTransaction();
        return dbSession;
    }

    public  void closeAndCommitSession(org.hibernate.Session dbSession){
        dbSession.getTransaction().commit();
        dbSession.close();
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }

        return sessionFactory;
    }
}
