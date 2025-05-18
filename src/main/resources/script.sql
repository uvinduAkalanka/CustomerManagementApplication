// SQL Scripts for schema creation and initial data

-- DDL Scripts

-- Create Sequence for IDs
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

-- Countries Table
CREATE TABLE IF NOT EXISTS countries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Cities Table
CREATE TABLE IF NOT EXISTS cities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    country_id BIGINT NOT NULL,
    FOREIGN KEY (country_id) REFERENCES countries(id),
    UNIQUE (name, country_id)
);

-- Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    nic_number VARCHAR(255) NOT NULL UNIQUE
);

-- Customer Mobile Numbers Table
CREATE TABLE IF NOT EXISTS customer_mobile_numbers (
    customer_id BIGINT NOT NULL,
    mobile_number VARCHAR(255) NOT NULL,
    PRIMARY KEY (customer_id, mobile_number),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Addresses Table
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city_id BIGINT,
    customer_id BIGINT,
    FOREIGN KEY (city_id) REFERENCES cities(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Family Relationships Table
CREATE TABLE IF NOT EXISTS family_relationships (
    customer_id BIGINT NOT NULL,
    family_member_id BIGINT NOT NULL,
    PRIMARY KEY (customer_id, family_member_id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (family_member_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_customers_nic ON customers(nic_number);
CREATE INDEX idx_cities_country ON cities(country_id);
CREATE INDEX idx_addresses_customer ON addresses(customer_id);

-- DML Script for initial data

-- Insert some sample countries
INSERT INTO countries (name) VALUES ('United States');
INSERT INTO countries (name) VALUES ('Canada');
INSERT INTO countries (name) VALUES ('United Kingdom');
INSERT INTO countries (name) VALUES ('Australia');
INSERT INTO countries (name) VALUES ('India');
INSERT INTO countries (name) VALUES ('Japan');

-- Insert some sample cities
INSERT INTO cities (name, country_id) VALUES ('New York', 1);
INSERT INTO cities (name, country_id) VALUES ('Los Angeles', 1);
INSERT INTO cities (name, country_id) VALUES ('Chicago', 1);
INSERT INTO cities (name, country_id) VALUES ('Toronto', 2);
INSERT INTO cities (name, country_id) VALUES ('Vancouver', 2);
INSERT INTO cities (name, country_id) VALUES ('London', 3);
INSERT INTO cities (name, country_id) VALUES ('Manchester', 3);
INSERT INTO cities (name, country_id) VALUES ('Sydney', 4);
INSERT INTO cities (name, country_id) VALUES ('Melbourne', 4);
INSERT INTO cities (name, country_id) VALUES ('Mumbai', 5);
INSERT INTO cities (name, country_id) VALUES ('Delhi', 5);
INSERT INTO cities (name, country_id) VALUES ('Tokyo', 6);
INSERT INTO cities (name, country_id) VALUES ('Osaka', 6);
