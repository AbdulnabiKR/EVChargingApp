import java.sql.*;

public class DBxample {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging";
    private static final String USER = "root";
    private static final String PASSWORD = "admin@1234";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected!");

            // 1️⃣ INSERT a new charging station
            String insertSQL = "INSERT INTO ChargingStation (name, latitude, longitude, available_slots) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, "City Power Hub");
            pstmt.setDouble(2, 13.0827);
            pstmt.setDouble(3, 80.2707);
            pstmt.setInt(4, 8);
            int inserted = pstmt.executeUpdate();
            if (inserted > 0) System.out.println("✅ Inserted new charging station.");

            // 2️⃣ SELECT all charging stations
            String selectSQL = "SELECT * FROM ChargingStation";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectSQL);
            System.out.println("\n🚗 Charging Stations in DB:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getDouble("latitude") + ", " +
                        rs.getDouble("longitude") + " | Slots: " +
                        rs.getInt("available_slots"));
            }

            // 3️⃣ SELECT all reservations
            String reservationSQL = "SELECT * FROM Reservation";
            rs = stmt.executeQuery(reservationSQL);
            System.out.println("\n📅 Reservations in DB:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | StationID: " +
                        rs.getInt("station_id") + " | UserID: " +
                        rs.getInt("user_id") + " | Time: " +
                        rs.getTimestamp("slot_time") + " | Status: " +
                        rs.getString("status"));
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

