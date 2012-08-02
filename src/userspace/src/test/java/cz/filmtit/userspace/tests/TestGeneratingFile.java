package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.GenerateFile;
import cz.filmtit.userspace.USHibernateUtil;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGeneratingFile {
    private MockFilmTitBackendServer server;
    @BeforeClass
    public static void InitializeDatabase() {
        Configuration configuration = new Configuration("configuration.xml");
        ConfigurationSingleton.setConf(configuration);
        MockHibernateUtil.changeUtilsInAllClasses();
    }

    private USHibernateUtil usHibernateUtil = MockHibernateUtil.getInstance();

    void TestGeneratingFile()
    {
        CreateServer();
    }



    @Test
    public void testGenerate()
    {
        System.out.println("TestGenerate");
        Session dbSession = usHibernateUtil.getSessionWithActiveTransaction();
        GenerateFile data = new GenerateFile();
        data.generateFile(0, GenerateFile.FileType.SRT,dbSession);
        usHibernateUtil.closeAndCommitSession(dbSession);
    }


    void CreateServer()
    {
        server = new MockFilmTitBackendServer();
    }

}


