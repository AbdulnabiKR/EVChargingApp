import java.sql.*;
import java.util.Scanner;

public class BookReservation {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging";
    private static final String USER = "root";
    private static final String PASSWORD = "admin@1234";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // Show stations
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ChargingStation");
            System.out.println("🚗 Available Stations:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " +
                        rs.getString("name") + " | Slots: " +
                        rs.getInt("available_slots"));
            }

            // Take inputs
            System.out.print("\nEnter station ID to book: ");
            int stationId = sc.nextInt();
            System.out.print("Enter your user ID: ");
            int userId = sc.nextInt();
            sc.nextLine(); // Consume newline
            System.out.print("Enter reservation time (YYYY-MM-DD HH:MM:SS): ");
            String slotTime = sc.nextLine();

            // Insert reservation
            String insertRes = "INSERT INTO Reservation (station_id, user_id, slot_time, status)" +
                    " VALUES (?, ?, ?, 'Booked')";
            PreparedStatement pstmt = conn.prepareStatement(insertRes);
            pstmt.setInt(1, stationId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, slotTime);
            int inserted = pstmt.executeUpdate();

            // Update available slots
            if (inserted > 0) {
                String updateSlots = "UPDATE ChargingStation " +
                        "SET available_slots = available_slots - 1 " +
                        "WHERE id = ? AND available_slots > 0";
                PreparedStatement pstmt2 = conn.prepareStatement(updateSlots);
                pstmt2.setInt(1, stationId);
                pstmt2.executeUpdate();
                System.out.println("✅ Reservation booked successfully!");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
