package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility.
 *
 * Reads JDBC settings from {@code db.properties} on the classpath
 * (located in the project's {@code resources/} folder).
 */
public final class DBConnection {

    private static final String PROPS_FILE = "db.properties";
    private static String url;
    private static String username;
    private static String password;

    static {
        loadProperties();
    }

    private DBConnection() { /* no instances */ }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPS_FILE)) {

            if (in == null) {
                throw new RuntimeException(
                    "Could not find " + PROPS_FILE + " on the classpath. " +
                    "Make sure 'resources' is a source folder in Eclipse.");
            }
            props.load(in);
            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");

            if (url == null || username == null || password == null) {
                throw new RuntimeException(
                    "db.properties is missing one of: db.url, db.username, db.password");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load " + PROPS_FILE, ex);
        }
    }

    /**
     * Open a fresh Connection. 
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /** Returns the configured JDBC URL. */
    public static String getUrl()      { return url; }
    public static String getUsername() { return username; }
}
