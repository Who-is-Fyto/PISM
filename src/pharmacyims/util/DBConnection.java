package pharmacyims.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Database connection manager for Fyto PIMS.
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

    // Loads connection properties from classpath or resources folder
    private static synchronized void loadConfiguration() {
        if (initialized) return;

        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
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
        username = props.getProperty("db.user", "fyto");
        password = props.getProperty("db.password", "#Chipapamike16");

        try {
            Class.forName(driver);
            initialized = true;
            LOGGER.info("JDBC Driver successfully registered: " + driver);
            LOGGER.info("Database configured for URL: " + url + " | DB User: " + username);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found on classpath!", e);
        }
    }

    // Returns a live JDBC Connection
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(url, username, password);
    }

    // Tests database connectivity without throwing exceptions
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
