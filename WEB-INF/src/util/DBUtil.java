package util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility. Uses Tomcat JDBC pool (JNDI) when available,
 * otherwise uses DriverManager with environment variables (no hardcoded credentials).
 * Set DB_URL, DB_USER, DB_PASSWORD when not using JNDI.
 */
public class DBUtil {
    private static final String JNDI_NAME = "java:comp/env/jdbc/GalaxyBBS";

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Oracle JDBC Driver not found. Please add ojdbc8.jar to WEB-INF/lib");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(JNDI_NAME);
            if (ds != null) {
                return ds.getConnection();
            }
        } catch (NamingException e) {
            // JNDI not available (e.g. running outside Tomcat) - fall through to DriverManager
        }

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (url == null || url.isEmpty() || user == null || user.isEmpty() || password == null) {
            throw new SQLException(
                "When JNDI is not used, DB_URL, DB_USER and DB_PASSWORD environment variables must be set.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
