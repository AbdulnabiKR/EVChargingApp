	CREATE DATABASE ev_charging;
	USE ev_charging;
	-- Create ChargingStation table
	CREATE TABLE ChargingStation (
		id INT PRIMARY KEY AUTO_INCREMENT,
		name VARCHAR(100) NOT NULL,
		latitude DOUBLE NOT NULL,
		longitude DOUBLE NOT NULL,
		available_slots INT DEFAULT 0
	);

	-- Create Reservation table
	CREATE TABLE Reservation (
		id INT PRIMARY KEY AUTO_INCREMENT,
		station_id INT NOT NULL,
		user_id INT NOT NULL,
		slot_time DATETIME NOT NULL,
		status VARCHAR(20) CHECK (status IN ('Booked', 'Cancelled')),
		CONSTRAINT fk_station
			FOREIGN KEY (station_id)
			REFERENCES ChargingStation(id)
			ON DELETE CASCADE
	);
	INSERT INTO ChargingStation (name, latitude, longitude, available_slots)
	VALUES ('Green Energy Station', 12.9716, 77.5946, 5);
	INSERT INTO Reservation (station_id, user_id, slot_time, status)
	VALUES (1, 101, '2025-08-13 10:00:00', 'Booked');
	SELECT * FROM ChargingStation ;
	SELECT * FROM Reservation;
    
  SELECT *, Round((6371 * acos(
    cos(radians(12.9716)) * cos(radians(latitude)) *
    cos(radians(longitude) - radians(77.5946)) +
    sin(radians(12.9716)) * sin(radians(latitude))
)),2) AS distance
FROM ChargingStation
ORDER BY distance ASC
LIMIT 5;

SELECT r.id AS reservation_id,
       cs.name AS station_name,
       r.slot_time,
       r.status
FROM Reservation r
JOIN ChargingStation cs ON r.station_id = cs.id
WHERE r.user_id = 1;

SELECT * FROM Reservation ORDER BY id DESC LIMIT 5;
SELECT * FROM ChargingStation WHERE id = 1 LIMIT 1000;	

SELECT * FROM Reservation WHERE id =3;
SELECT * FROM ChargingStation WHERE id = 1;
SELECT * FROM Reservation WHERE id = 5;
SET SQL_SAFE_UPDATES=0;
UPDATE Reservation SET status = 'Completed' 
WHERE status = 'Booked' AND slot_time < NOW();

SELECT r.id, cs.name, r.slot_time, r.status
FROM Reservation r
JOIN ChargingStation cs ON r.station_id = cs.id
WHERE r.user_id = 123
  AND (r.status = 'Cancelled' OR r.status = 'Completed' OR r.status = 'Booked')
ORDER BY r.slot_time DESC;












