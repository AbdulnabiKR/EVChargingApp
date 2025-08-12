import java.sql.*;
import java.util.Scanner;

public class CancelReservation {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging";
    private static final String USER = "root";
    private static final String PASSWORD = "admin@1234";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // Show all active (Booked) reservations
            String showSQL = "SELECT id, station_id, user_id, slot_time, status FROM Reservation WHERE status = 'Booked'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(showSQL);

            System.out.println("📅 Active Reservations:");
            boolean hasAny = false;
            while (rs.next()) {
                hasAny = true;
                System.out.println(rs.getInt("id") + " | Station ID: " +
                        rs.getInt("station_id") + " | User ID: " +
                        rs.getInt("user_id") + " | Time: " +
                        rs.getTimestamp("slot_time") + " | Status: " +
                        rs.getString("status"));
            }

            if (!hasAny) {
                System.out.println("❌ No active reservations found.");
                conn.close();
                return;
            }

            // Get reservation ID from user
            System.out.print("\nEnter the Reservation ID you want to cancel: ");
            int reservationId = sc.nextInt();

            // Find the station_id for this reservation
            String findStationSQL = "SELECT station_id FROM Reservation WHERE id = ?";
            PreparedStatement pstmtFind = conn.prepareStatement(findStationSQL);
            pstmtFind.setInt(1, reservationId);
            ResultSet rsFind = pstmtFind.executeQuery();

            if (!rsFind.next()) {
                System.out.println("❌ Reservation not found.");
                conn.close();
                return;
            }
            int stationId = rsFind.getInt("station_id");

            // Update reservation status to 'Cancelled'
            String cancelSQL = "UPDATE Reservation SET status = 'Cancelled' WHERE id = ?";
            PreparedStatement pstmtCancel = conn.prepareStatement(cancelSQL);
            pstmtCancel.setInt(1, reservationId);
            int updated = pstmtCancel.executeUpdate();

            if (updated > 0) {
                // Increase station's available slots
                String updateSlots = "UPDATE ChargingStation SET available_slots = available_slots + 1 WHERE id = ?";
                PreparedStatement pstmtSlots = conn.prepareStatement(updateSlots);
                pstmtSlots.setInt(1, stationId);
                pstmtSlots.executeUpdate();

                System.out.println("✅ Reservation cancelled and slot freed successfully!");
            } else {
                System.out.println("❌ Could not cancel reservation.");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
