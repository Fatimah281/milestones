# Milestone 04 – Logging, JSON Schema Validation & Exception Handling (Servlet V4)

## Overview
This milestone enhances the Employee RESTful API (Version 4) with **Log4j2** logging, **JSON Schema validation** (FasterXML/Jackson), and **structured exception handling**.

Benefits:
- Better debugging and monitoring
- Centralized exception handling
- Request payload validation before processing
- Production-ready logging

All APIs are RESTful and tested with Postman.

---

## Technologies
- Java (JDK) · Servlet / J2EE · Apache Tomcat · IBM DB2
- JDBC · JNDI Connection Pooling
- Apache Log4j2 · FasterXML (Jackson)
- RESTful API · Postman

---

## Features

### 1. Logging (Log4j2)
- **Config:** `log4j2.xml` with levels TRACE, DEBUG, INFO, WARN, ERROR, FATAL
- **Where:** Controllers, Service, DAO, exception handling
- **Output:** Console + rolling file appender  
→ Tracks API calls, request data, DB operations, and errors.

### 2. JSON Schema Validation (Jackson)
- **Schema:** Defined for Employee object
- **Flow:** Incoming JSON validated before processing
- **On failure:** HTTP 400 (Bad Request) + validation error logged
- **Checks:** Required fields, data types, formats, arrays (e.g. hobbies)  
→ Stops invalid data from reaching business logic or DB.

---

## How to Run
1. Open project in IntelliJ / Eclipse / STS
2. Configure DB2 (JNDI connection pool)
3. Deploy on Apache Tomcat
4. Test APIs with Postman

---

## Postman
[Milestone 04 Collection](https://.postman.co/workspace/Personal-Workspace~7cb8a6f3-a006-49c7-b28a-f592e7b4b171/request/33418576-e15f8d93-5a15-4c18-b108-2a6ade28486f?action=share&creator=33418576)
