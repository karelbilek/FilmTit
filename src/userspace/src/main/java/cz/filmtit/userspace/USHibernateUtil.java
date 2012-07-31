package cz.filmtit.userspace;

import cz.filmtit.core.CoreHibernateUtil;

public class USHibernateUtil extends CoreHibernateUtil {

    // use the user space path instead of the original core one
    public static java.net.URL configurationFile = USHibernateUtil.class.getResource("/cz/filmtit/userspace/userspace.cfg.xml");

}
