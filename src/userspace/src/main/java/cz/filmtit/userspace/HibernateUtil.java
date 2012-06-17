package cz.filmtit.userspace;

import cz.filmtit.core.ConfigurationSingleton;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {
    private static SessionFactory sessionFactory = null;
    private static ServiceRegistry serviceRegistry;

    //this is fine, since it uses getResource
    public static java.net.URL configurationFile = HibernateUtil.class.getResource("/cz/filmtit/userspace/hibernate.cfg.xml");

    /**
     * A path to the Hibernate configuration file. If it's necessary to change it (e.g. for unit testing),
     * it has to be done using reflection before the getSessionFactory method is called for the first time.
     */
    private static SessionFactory buildSessionFactory() {
        try {
            cz.filmtit.core.Configuration projectConfiguration = ConfigurationSingleton.getConf();

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

    public static void buildSessionFactoryFromHbmFile(String hbmFileName) {
        //java.net.URL configurationFile = HibernateUtil.class.getResource(hbmFileName);

        Configuration configuration = new Configuration();
        //configuration.configure(configurationFile);
        configuration.configure(hbmFileName);


        serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    //TODO : free the lock after some time
    //TODO : isn't there some already done solution?
    private static boolean blocking = false;
    private static synchronized boolean lock(boolean opening) {
        if (opening) {
            if(blocking == true) {
                return true;
            } else {
                blocking = true;
                return false;
            }
        } else {
            blocking = false;
            return true; //return here does not matter
        }
    }
    public static org.hibernate.Session getCurrentSession() {
        while(lock(true)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("Interrupted exception "+e);
            }
        }
        org.hibernate.Session dbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        dbSession.beginTransaction();
        return dbSession;
    }

    public static void closeAndCommitSession(org.hibernate.Session dbSession){
       dbSession.getTransaction().commit();
       lock(false);
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }
}
