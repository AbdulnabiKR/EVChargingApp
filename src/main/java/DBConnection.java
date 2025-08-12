
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database connection details
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging"; // database name
    private static final String USER = "root"; // your MySQL username
    private static final String PASSWORD = "admin@1234"; // your MySQL password

    public static void main(String[] args) {
        try {
            // STEP 1: Load MySQL JDBC Driver (not always required in new versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // STEP 2: Connect to Database
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            if (conn != null) {
                System.out.println("✅ Connected to the database successfully!");
                conn.close(); // Close connection after use
            }

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
