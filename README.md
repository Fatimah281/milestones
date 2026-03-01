# Milestone 04 - Employee REST API with DB2 (JPA/Hibernate V4)

## Overview
This milestone extends the Employee RESTful API by migrating the database integration from JDBC to **JPA** using **Hibernate** as the implementation.  
The project now uses ORM instead of manual SQL queries, providing cleaner and more maintainable code.

This milestone also introduces **transaction management**, ensuring data consistency and rollback handling during CRUD operations and employee-hobby relationship updates.

**Milestone 4** adds:
- **Log4j 2** for structured logging across all layers (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- **FasterXML (Jackson)** for JSON processing and schema-based validation
- **Exception handling** with custom exceptions and consistent API error responses

All APIs follow RESTful principles and are tested using Postman.

---

## Technologies
- Java (JDK)
- Servlet / J2EE
- Apache Tomcat
- IBM DB2
- JPA (Java Persistence API)
- Hibernate (JPA Implementation)
- Transaction Management
- **Log4j 2** – logging (all levels: TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- **FasterXML Jackson** – JSON serialization/deserialization and JSON Schema validation
- **Exception handling** – custom exceptions (`ResourceNotFoundException`, `ValidationException`, `InvalidJsonException`) and `ApiError` JSON responses
- RESTful API Principles
- Postman

---

## Log4j 2
- **Configuration:** `src/main/resources/log4j2.xml`
- **Appenders:** Console (INFO+), rolling file (INFO+), diagnostic file (TRACE+ for debugging)
- **Loggers:** `ServletController`, `service`, `DAO`, `util`, `exception`
- Logs are written to the `logs/` directory (e.g. `servlet_employee.log`, `servlet_employee-diagnostic.log`)

---

## FasterXML (Jackson)
- **JSON processing:** `util.JsonUtil` uses Jackson `ObjectMapper` for:
  - `fromJson()` / `toJson()` – serialization and deserialization
  - `readTree()` – parse to `JsonNode` for validation
- **JSON Schema validation:** Request bodies (POST/PUT) are validated against `src/main/resources/schemas/employee.schema.json` (draft-07) before deserialization
- **Method:** `fromJsonWithEmployeeValidation(Reader, Class)` – validates then deserializes; invalid or non-conformant JSON returns 400 with validation details

---

## Exception Handling
- **Custom exceptions** (package `exception`):
  - `ResourceNotFoundException` → HTTP 404
  - `ValidationException` → HTTP 400 (with optional `details` list for schema errors)
  - `InvalidJsonException` → HTTP 400 (malformed or invalid JSON)
- **API errors:** All error responses use the `ApiError` DTO (`error`, `message`, optional `details`) as JSON
- **Central handling:** `EmployeeServlet` maps exceptions to status codes and logs errors; stack traces are never exposed to clients

---

## How to Run
1. Open the project in **IntelliJ / Eclipse / STS**
2. Configure DB2 connection (Hibernate / JPA configuration)
3. Deploy and run the project on **Apache Tomcat**
4. Test all APIs using **Postman**

---

## Postman Testing
The Postman Collection for testing all APIs is available here:  
[View Milestone 04 Collection](https://.postman.co/workspace/Personal-Workspace~7cb8a6f3-a006-49c7-b28a-f592e7b4b171/request/33418576-e15f8d93-5a15-4c18-b108-2a6ade28486f?action=share&creator=33418576)
