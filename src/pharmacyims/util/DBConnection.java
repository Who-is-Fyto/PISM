package pharmacyims.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe Database Connection Manager for Fyto PIMS.
 * Reads credentials from db.properties and manages JDBC connections.
 */
public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static String url;
    private static String username;
    private static String password;
    private static String driver;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    private static synchronized void loadConfiguration() {
        if (initialized) return;

        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                // Fallback to checking direct file path
                java.io.File file = new java.io.File("resources/db.properties");
                if (file.exists()) {
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                        props.load(fis);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load db.properties, using defaults.", e);
        }

        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/pims_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        username = props.getProperty("db.user", "root");
        password = props.getProperty("db.password", "root");

        try {
            Class.forName(driver);
            initialized = true;
            LOGGER.info("JDBC Driver successfully registered: " + driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found on classpath!", e);
        }
    }

    /**
     * Obtains a live JDBC Connection to MySQL.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Non-throwing test of database connectivity.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static void setCredentials(String newUrl, String newUser, String newPass) {
        url = newUrl;
        username = newUser;
        password = newPass;
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
