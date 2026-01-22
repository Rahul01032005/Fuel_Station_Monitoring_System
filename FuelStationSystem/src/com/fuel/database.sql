CREATE DATABASE fuel_station;
USE fuel_station;

-- Fuel Tank Table
CREATE TABLE fuel_tank (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fuel_type VARCHAR(20) UNIQUE,
    price_per_liter DECIMAL(6,2),
    available_liters DECIMAL(10,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP
);

-- Admin Refill History
CREATE TABLE refill_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fuel_id INT,
    refill_liters DECIMAL(10,2),
    refill_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fuel_id) REFERENCES fuel_tank(id)
);

-- Sales Log (Worker)
CREATE TABLE sales_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fuel_id INT,
    sold_liters DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    sale_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fuel_id) REFERENCES fuel_tank(id)
);

-- Initial Data
INSERT INTO fuel_tank (fuel_type, price_per_liter, available_liters) VALUES
('Petrol', 102.50, 500),
('Diesel', 92.30, 600),
('CNG', 78.00, 300);
