package util;
//<editor-fold desc="Imports">
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
//</editor-fold>

/**
 * Provides the DataSource from JNDI (connection pooling).
 */
public final class DataSourceProvider {

    //<editor-fold desc="Constants">
    private static final String JNDI_NAME = "java:comp/env/jdbc/EmployeeDB";
    //</editor-fold>

    //<editor-fold desc="Constructors">
    private DataSourceProvider() {
    }
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public static DataSource getDataSource() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (DataSource) ctx.lookup(JNDI_NAME);
    }
    //</editor-fold>
}
