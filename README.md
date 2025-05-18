Customer Management System
A comprehensive customer management application built with Spring Boot and React.
Backend Technologies

Java 17
Spring Boot 2.7.x
MariaDB
JUnit 5
Maven
Apache POI (for Excel processing)

Features

Customer CRUD operations
Customer relationship management (family members)
Address management with city and country handling
Bulk customer creation via Excel file upload
Performance optimizations for large datasets

Project Structure
customer-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── customer/
│   │   │           └── management/
│   │   │               ├── controller/
│   │   │               ├── dto/
│   │   │               ├── exception/
│   │   │               ├── model/
│   │   │               ├── repository/
│   │   │               ├── service/
│   │   │               ├── util/
│   │   │               └── CustomerManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── sql/
│   │           ├── schema.sql
│   │           └── data.sql
│   └── test/
│       └── java/
│           └── com/
│               └── customer/
│                   └── management/
│                       ├── controller/
│                       ├── repository/
│                       └── service/
└── pom.xml
Database Schema
The database schema consists of the following tables:

customers - Main customer information
customer_mobile_numbers - Customer mobile numbers (one-to-many)
family_relationships - Customer family relationships (many-to-many)
addresses - Customer addresses (one-to-many)
cities - City master data (referenced by addresses)
countries - Country master data (referenced by cities)

Getting Started
Prerequisites

JDK 17
Maven 3.8+
MariaDB 10.5+
Node.js 16+ (for frontend)

Database Setup

Create a MariaDB database:

sqlCREATE DATABASE customer_management;

Configure database credentials in application.properties.

Backend Setup

Clone the repository
Navigate to the project directory
Build the project:

bashmvn clean install

Run the application:

bashmvn spring-boot:run
The backend server will start on port 8080.
API Endpoints
Customer Endpoints

GET /api/customers - Retrieve all customers (paginated)
GET /api/customers/{id} - Retrieve a specific customer
POST /api/customers - Create a new customer
PUT /api/customers/{id} - Update an existing customer
DELETE /api/customers/{id} - Delete a customer
POST /api/customers/bulk-upload - Upload bulk customer data via Excel

Bulk Processing Approach
The system handles large Excel files (potentially with millions of records) by:

Using asynchronous processing with Spring's @Async annotation
Processing data in batches (1000 records at a time)
Using Apache POI's streaming API (SXSSF) to minimize memory usage
Implementing timeout configurations to prevent request timeouts
Configuring thread pool parameters for optimal performance
Returning an immediate response to the client while processing continues in background

Testing
Run the tests using:
bashmvn test
Future Enhancements

Add user authentication and authorization
Implement audit logging
Add reporting functionality
Create a dashboard with customer analytics
Implement real-time data validation