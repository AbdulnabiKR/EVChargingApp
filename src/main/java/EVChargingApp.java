import java.sql.*;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class EVChargingApp {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ev_charging";
    private static final String USER = "root";
    private static final String PASSWORD = "admin@1234";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            while (true) { // loop until user chooses Exit
                System.out.println("\n==== EV Charging Station App ====");
                System.out.println("1. View all stations");
                System.out.println("2. Add new station");
                System.out.println("3. Book a reservation");
                System.out.println("4. Cancel a reservation");
                System.out.println("5. Find nearest stations");
                System.out.println("6. View my reservations");
                System.out.println("7. View booking history (past reservations)");
                System.out.println("8. Exit");
                System.out.print("Enter choice: ");
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline

                switch (choice) {
                    case 1 -> viewStations();
                    case 2 -> addStation(sc);
                    case 3 -> bookReservation(sc);
                    case 4 -> cancelReservation(sc);
                    case 5 -> findNearestStations(sc);
                    case 6 -> viewMyReservations(sc);
                    case 7 -> viewBookingHistory(sc);

                    case 8 -> {
                        System.out.println("👋 Exiting program. Goodbye!");
                        return; // exits the main method
                    }
                    default -> System.out.println("❌ Invalid choice, please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //1.View all stations
    private static void viewStations() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM ChargingStation")) {

            System.out.println("\n🚗 Charging Stations:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getDouble("latitude") + ", " +
                        rs.getDouble("longitude") + " | Slots: " +
                        rs.getInt("available_slots"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //2.Add station
    private static void addStation(Scanner sc) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.print("Enter station name: ");
            String name = sc.nextLine();
            System.out.print("Enter latitude: ");
            double lat = sc.nextDouble();
            System.out.print("Enter longitude: ");
            double lng = sc.nextDouble();
            System.out.print("Enter available slots: ");
            int slots = sc.nextInt();
            sc.nextLine(); // consume newline

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //3.Book reservation
    private static void bookReservation(Scanner sc) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Show stations list neatly
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ChargingStation");
            System.out.printf("%-5s %-25s %-12s %-12s %-8s%n",
                    "ID", "Name", "Latitude", "Longitude", "Slots");
            System.out.println("-----------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d %-25s %-12.5f %-12.5f %-8d%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("available_slots"));
            }

            // Input station ID
            System.out.print("Enter station ID to book: ");
            int stationId = sc.nextInt();

            // Check available slots
            String checkSlotsSQL = "SELECT available_slots FROM ChargingStation WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSlotsSQL);
            checkStmt.setInt(1, stationId);
            ResultSet slotRS = checkStmt.executeQuery();
            if (slotRS.next() && slotRS.getInt("available_slots") <= 0) {
                System.out.println("❌ No slots available for this station!");
                return;
            }

            // User ID
            System.out.print("Enter your user ID: ");
            int userId = sc.nextInt();
            sc.nextLine(); // consume newline

            // Reservation time with date/time validation
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            System.out.print("Enter reservation time (YYYY-MM-DD HH:MM:SS): ");
            String slotTimeStr = sc.nextLine();

            LocalDateTime slotTime;
            try {
                slotTime = LocalDateTime.parse(slotTimeStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date/time format! Use YYYY-MM-DD HH:MM:SS");
                return;
            }

            if (slotTime.isBefore(now)) {
                System.out.println("❌ Cannot book a slot in the past!");
                return;
            }

            // Insert reservation
            String insertSQL = "INSERT INTO Reservation (station_id, user_id, slot_time, status) VALUES (?, ?, ?, 'Booked')";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setInt(1, stationId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, slotTimeStr);
            int inserted = pstmt.executeUpdate();

            if (inserted > 0) {
                String updateSlots = "UPDATE ChargingStation SET available_slots = available_slots - 1 WHERE id = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(updateSlots);
                pstmt2.setInt(1, stationId);
                pstmt2.executeUpdate();
                System.out.println("✅ Reservation booked successfully!");

                // After-action prompt
                System.out.print("View your reservations now? (y/n): ");
                String viewChoice = sc.next();
                if (viewChoice.equalsIgnoreCase("y")) {
                    viewMyReservations(sc);
                }
            } else {
                System.out.println("❌ Could not book reservation.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //4.Cancel reservation
    private static void cancelReservation(Scanner sc) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.print("\nEnter the Reservation ID you want to cancel: ");
            int reservationId = sc.nextInt();

            // JOIN to get station details
            String stationDetailsSQL = """
            SELECT cs.*
            FROM ChargingStation cs
            JOIN Reservation r ON cs.id = r.station_id
            WHERE r.id = ?;
        """;
            PreparedStatement pstmtStation = conn.prepareStatement(stationDetailsSQL);
            pstmtStation.setInt(1, reservationId);
            ResultSet rsStation = pstmtStation.executeQuery();

            if (!rsStation.next()) {
                System.out.println("❌ Reservation not found.");
                return;
            }

            System.out.println("\n🔹 Station Details for this reservation:");
            System.out.printf("%d | %s | %.5f, %.5f | Slots: %d%n",
                    rsStation.getInt("id"),
                    rsStation.getString("name"),
                    rsStation.getDouble("latitude"),
                    rsStation.getDouble("longitude"),
                    rsStation.getInt("available_slots")
            );

            int stationId = rsStation.getInt("id");

            // Confirm before cancelling
            System.out.print("Do you want to cancel this reservation? (y/n): ");
            String confirm = sc.next();
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("❌ Cancellation aborted.");
                return;
            }

            // Update reservation status to Cancelled
            String cancelSQL = "UPDATE Reservation SET status = 'Cancelled' WHERE id = ?";
            PreparedStatement pstmtCancel = conn.prepareStatement(cancelSQL);
            pstmtCancel.setInt(1, reservationId);
            int updated = pstmtCancel.executeUpdate();

            if (updated > 0) {
                // Free up a slot in the ChargingStation table
                String updateSlots = "UPDATE ChargingStation SET available_slots = available_slots + 1 WHERE id = ?";
                PreparedStatement pstmtSlots = conn.prepareStatement(updateSlots);
                pstmtSlots.setInt(1, stationId);
                pstmtSlots.executeUpdate();
                System.out.println("✅ Reservation cancelled and slot freed successfully!");

                // After-action prompt
                System.out.print("Do you want to book another station now? (y/n): ");
                String choice = sc.next();
                if (choice.equalsIgnoreCase("y")) {
                    bookReservation(sc);
                }
            } else {
                System.out.println("❌ Could not cancel reservation.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//5.findnearest station
    private static void findNearestStations(Scanner sc) {
        String sql = """
        SELECT *, (6371 * acos(
            cos(radians(?)) * cos(radians(latitude)) *
            cos(radians(longitude) - radians(?)) +
            sin(radians(?)) * sin(radians(latitude))
        )) AS distance
        FROM ChargingStation
        WHERE available_slots > 0
        ORDER BY distance ASC
        LIMIT 5;
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.print("Enter your current latitude: ");
            double userLat = sc.nextDouble();
            System.out.print("Enter your current longitude: ");
            double userLng = sc.nextDouble();

            pstmt.setDouble(1, userLat);
            pstmt.setDouble(2, userLng);
            pstmt.setDouble(3, userLat);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nNearest Charging Stations:");
            while (rs.next()) {
                double distance = rs.getDouble("distance");
                distance = Math.round(distance * 100.0) / 100.0; // rounds to 2 decimals
                System.out.printf("%d | %s | (%.5f, %.5f) | Slots: %d | Distance: %.2f km%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("available_slots"),
                        rs.getDouble("distance"));
            }
            System.out.print("Do you want to book from these stations? (y/n): ");
            String choice = sc.next();
            if(choice.equalsIgnoreCase("y")) {
                bookReservation(sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //6.Veiw my reservations
    private static void viewMyReservations(Scanner sc) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.print("Enter your user ID: ");
            int userId = sc.nextInt();

            // === ACTIVE BOOKINGS ===
            String activeSQL = """
            SELECT r.id, cs.name, r.slot_time, r.status
            FROM Reservation r
            JOIN ChargingStation cs ON r.station_id = cs.id
            WHERE r.user_id = ?
              AND r.status = 'Booked'
            ORDER BY r.slot_time ASC
        """;
            PreparedStatement pstmtActive = conn.prepareStatement(activeSQL);
            pstmtActive.setInt(1, userId);
            ResultSet rsActive = pstmtActive.executeQuery();

            System.out.println("\n📌 ACTIVE BOOKINGS:");
            System.out.printf("%-5s %-25s %-20s %-12s%n",
                    "ID", "Station Name", "Slot Time", "Status");
            System.out.println("--------------------------------------------------------------");

            boolean activeFound = false;
            while (rsActive.next()) {
                activeFound = true;
                System.out.printf("%-5d %-25s %-20s %-12s%n",
                        rsActive.getInt("id"),
                        rsActive.getString("name"),
                        rsActive.getString("slot_time"),
                        rsActive.getString("status"));
            }
            if (!activeFound) {
                System.out.println("No active bookings found.");
            }

            // === PAST BOOKINGS ===
            String pastSQL = """
            SELECT r.id, cs.name, r.slot_time, r.status
            FROM Reservation r
            JOIN ChargingStation cs ON r.station_id = cs.id
            WHERE r.user_id = ?
              AND (r.status = 'Cancelled' OR r.status = 'Completed')
            ORDER BY r.slot_time DESC
        """;
            PreparedStatement pstmtPast = conn.prepareStatement(pastSQL);
            pstmtPast.setInt(1, userId);
            ResultSet rsPast = pstmtPast.executeQuery();

            System.out.println("\n📜 PAST BOOKINGS:");
            System.out.printf("%-5s %-25s %-20s %-12s%n",
                    "ID", "Station Name", "Slot Time", "Status");
            System.out.println("--------------------------------------------------------------");

            boolean pastFound = false;
            while (rsPast.next()) {
                pastFound = true;
                System.out.printf("%-5d %-25s %-20s %-12s%n",
                        rsPast.getInt("id"),
                        rsPast.getString("name"),
                        rsPast.getString("slot_time"),
                        rsPast.getString("status"));
            }
            if (!pastFound) {
                System.out.println("No past bookings found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//7.View past booking history
    private static void viewBookingHistory(Scanner sc) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // ✅ Step 1: Auto‑mark past Booked reservations as Completed
            String completeSQL =
                    "UPDATE Reservation SET status = 'Completed' " +
                            "WHERE status = 'Booked' AND slot_time < NOW()";
            conn.prepareStatement(completeSQL).executeUpdate();

            // ✅ Step 2: Proceed with history display
            System.out.print("Enter your user ID to view booking history: ");
            int userId = sc.nextInt();

            String sql = """
            SELECT r.id, cs.name, r.slot_time, r.status
            FROM Reservation r
            JOIN ChargingStation cs ON r.station_id = cs.id
            WHERE r.user_id = ?
                 AND (r.status = 'Cancelled' OR r.status = 'Completed' OR r.status = 'Booked')
            ORDER BY r.slot_time DESC
        """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.printf("\n%-5s %-25s %-20s %-12s%n",
                    "ID", "Station Name", "Slot Time", "Status");
            System.out.println("--------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-25s %-20s %-12s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("slot_time"),
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No past reservations found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

