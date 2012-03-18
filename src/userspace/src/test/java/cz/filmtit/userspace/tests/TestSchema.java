package cz.filmtit.userspace.tests;

import cz.filmtit.userspace.*;
import cz.filmtit.userspace.Session;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import java.lang.reflect.*;

public class TestSchema {
  /*  static {
        Configuration config = new Configuration()
                .setProperty("hibernate.dialect","org.hibernate.dialect.HSQLDialect")
                .setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver")
                .setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:baseball")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.connection.pool_size", "1")
                .setProperty("hibernate.connection.autocommit", "true")
                .setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .addClass(Translation.class)
                .addClass(Match.class)
                .addClass(Chunk.class)
                .addClass(Document.class)
                .addClass(User.class)
                .addClass(Session.class);

        System.out.println("The database initialization has just run.");
        
        try {
            Field field = HibernateUtil.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            field.set(null, config.buildSessionFactory());
        }
        catch (Exception e) {}
    }  */
}

// http://www.theserverside.com/news/1365222/Unit-Testing-Hibernate-With-HSQLDB