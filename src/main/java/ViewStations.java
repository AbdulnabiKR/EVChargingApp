import java.sql.*;

public class ViewStations {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging";
    private static final String USER = "root";
    private static final String PASSWORD = "admin@1234";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement();

            String sql = "SELECT * FROM ChargingStation";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("🚗 Charging Stations:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getDouble("latitude") + ", " +
                        rs.getDouble("longitude") + " | Slots: " +
                        rs.getInt("available_slots"));
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

