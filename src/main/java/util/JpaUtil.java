package util;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//</editor-fold>

public final class JpaUtil {

    //<editor-fold desc="Constants">
    private static final Logger LOG = LogManager.getLogger(JpaUtil.class);
    private static final String PERSISTENCE_UNIT_NAME = "EmployeePU";
    private static volatile EntityManagerFactory emf;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private JpaUtil() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JpaUtil.class) {
                if (emf == null) {
                    LOG.debug("Creating EntityManagerFactory for persistence unit: {}", PERSISTENCE_UNIT_NAME);
                    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    LOG.info("EntityManagerFactory initialized successfully");
                }
            }
        }
        return emf;
    }
    //</editor-fold>
}
