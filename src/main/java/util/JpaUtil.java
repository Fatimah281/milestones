package util;
//<editor-fold desc="Imports">
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
//</editor-fold>

public final class JpaUtil {

    //<editor-fold desc="Constants">
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
                    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                }
            }
        }
        return emf;
    }
    //</editor-fold>
}
