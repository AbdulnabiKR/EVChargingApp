import java.sql.*;
import java.util.Scanner;

public class AddStationInteractive {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging";
    private static final String USER = "root";
    private static final String PASSWORD = "admin@1234";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.print("Enter station name: ");
            String name = sc.nextLine();

            System.out.print("Enter latitude: ");
            double lat = sc.nextDouble();

            System.out.print("Enter longitude: ");
            double lng = sc.nextDouble();

            System.out.print("Enter available slots: ");
            int slots = sc.nextInt();

            String sql = "INSERT INTO ChargingStation (name, latitude, longitude, available_slots) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setDouble(2, lat);
            pstmt.setDouble(3, lng);
            pstmt.setInt(4, slots);

            int inserted = pstmt.executeUpdate();
            if (inserted > 0) {
                System.out.println("✅ Station added successfully!");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

