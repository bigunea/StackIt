package main;

import util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Standalone MySQL connection test.
 
public class DBTest {

    private static final String[] EXPECTED_TABLES =
        { "users", "budgets", "budget_items" };

    public static void main(String[] args) {
        
        System.out.println("  StackIt – MySQL Connection Diagnostic");
        System.out.println();

        // Step 1.Driver
        System.out.print("[1/5] Loading MySQL JDBC driver... ");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("OK");
        } catch (ClassNotFoundException ex) {
            System.out.println("FAILED");
            System.err.println("      → " + ex.getMessage());
            System.err.println("      → Is mysql-connector-j-x.x.x.jar in your build path?");
            return;
        }

        // Step 2.Properties
        System.out.print("[2/5] Reading db.properties... ");
        String url, user;
        try {
            url  = DBConnection.getUrl();
            user = DBConnection.getUsername();
            System.out.println("OK");
            System.out.println("      URL:  " + url);
            System.out.println("      User: " + user);
        } catch (Exception ex) {
            System.out.println("FAILED");
            System.err.println("      → " + ex.getMessage());
            return;
        }

        // Step 3.Connection
        System.out.print("[3/5] Opening connection... ");
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("OK");
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("      Server: " + meta.getDatabaseProductName()
                               + " " + meta.getDatabaseProductVersion());
            System.out.println("      Driver: " + meta.getDriverName()
                               + " " + meta.getDriverVersion());

            // Step 4.Tables
            System.out.print("[4/5] Checking required tables... ");
            List<String> missing = findMissingTables(conn);
            if (missing.isEmpty()) {
                System.out.println("OK");
                System.out.println("      Found: "
                    + String.join(", ", EXPECTED_TABLES));
            } else {
                System.out.println("FAILED");
                System.err.println("      → Missing table(s): "
                    + String.join(", ", missing));
                System.err.println("      → Run schema.sql in MySQL Workbench first.");
                return;
            }

            // Step 5.Row counts
            System.out.println("[5/5] Counting rows in each table........");
            for (String table : EXPECTED_TABLES) {
                int n = countRows(conn, table);
                System.out.printf("      %-15s %d row(s)%n", table, n);
            }

            System.out.println();
            System.out.println(" ALL CHECKS PASSED");
           

        } catch (SQLException ex) {
            System.out.println("FAILED");
            System.err.println();
            System.err.println("      → " + ex.getClass().getSimpleName()
                               + ": " + ex.getMessage());
            System.err.println();
            diagnoseSqlError(ex);
        }
    }

    private static List<String> findMissingTables(Connection conn) throws SQLException {
        List<String> missing = new ArrayList<>(Arrays.asList(EXPECTED_TABLES));
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%",
                                           new String[]{"TABLE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME").toLowerCase();
                missing.remove(name);
            }
        }
        return missing;
    }

    private static int countRows(Connection conn, String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void diagnoseSqlError(SQLException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        System.err.println("  Likely fix:");
        if (msg.contains("access denied")) {
            System.err.println("    → Wrong username or password in db.properties.");
            System.err.println("    → Check db.username and db.password values.");
        } else if (msg.contains("communications link failure") ||
                   msg.contains("connection refused")) {
            System.err.println("    → MySQL server is not running.");
            System.err.println("    → Start MySQL via System Settings on Mac, or run:");
            System.err.println("        brew services start mysql");
        } else if (msg.contains("unknown database")) {
            System.err.println("    → The 'stackit' database does not exist yet.");
            System.err.println("    → Open MySQL Workbench and run schema.sql.");
        } else if (msg.contains("no suitable driver")) {
            System.err.println("    → MySQL Connector JAR not on the classpath.");
            System.err.println("    → Right-click project → Build Path → Configure Build Path");
            System.err.println("    → Libraries → Add External JARs → mysql-connector-j-X.X.X.jar");
        } else {
            System.err.println("    → Read the error message above and check db.properties.");
        }
    }
}
