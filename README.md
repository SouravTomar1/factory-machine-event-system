# 🏭 Factory Machine Event Processing System

## 📌 Project Overview

This project is a **backend system** built using **Java** and **Spring Boot** that simulates how a factory collects, validates, and analyzes data from machines.

In a real-world factory, machines continuously produce items and send **events** to a backend system. These events can sometimes be duplicated, delayed, or corrected later. This service is designed to:

* Reliably ingest large batches of machine events
* Validate and deduplicate incoming data
* Handle concurrent requests safely
* Provide fast querying of machine statistics

---

## 🧱 Architecture

The application follows a **layered architecture**:

```
Controller Layer → Service Layer → Repository Layer → Database
        |
        v
      REST APIs
```

**Layers:**

* **Controller Layer:**
  Exposes REST APIs for event ingestion and statistics. Handles request/response mapping.

* **Service Layer:**
  Implements core business logic, including validation, deduplication, updates, and statistics calculations. Ensures transactional and concurrent-safe operations.

* **Repository Layer:**
  Uses JPA to interact with the database. Handles persistence only.

* **Database:**
  Stores events and enforces uniqueness and consistency constraints.

---

## 🗂️ Data Model

Each machine event is stored as a single record in the database.

**Event Entity (core fields):**

* `eventId` (unique identifier)
* `eventTime` (used for queries)
* `receivedTime` (set by service)
* `machineId`
* `factoryId`
* `lineId`
* `durationMs`
* `defectCount`

The database enforces a **unique constraint** on `eventId` to ensure deduplication.

---

## 🔁 Deduplication & Update Logic

When a new event arrives, the system checks for an existing event with the same `eventId`:

1. **No existing event:**
   → The event is stored as a new record.

2. **Existing event with identical payload:**
   → The event is ignored and counted as **deduplicated**.

3. **Existing event with different payload:**

   * If the incoming `receivedTime` is newer → update the stored event.
   * If the incoming `receivedTime` is older → ignore the event.

**Payload comparison** checks relevant fields such as `eventTime`, `machineId`, `durationMs`, and `defectCount`.
This simulates real-world scenarios where machines resend or correct messages.

---

## ✅ Validation Rules

An event is **rejected** if:

* `durationMs` < 0 or > 6 hours
* `eventTime` is more than 15 minutes in the future

**Special rule:**

* `defectCount = -1` means “unknown”. Stored but **ignored in defect calculations**.

Rejected events are **not saved** and are returned with **rejection reasons**.

---

## 🔒 Thread Safety

Designed for multiple machines sending events simultaneously.

Thread safety is ensured via:

* Database-level **unique constraints** on `eventId`
* **Transactional service methods**
* **Atomic update logic**
* Controlled batch processing to prevent race conditions

---

## 📊 Statistics & Query APIs

### 1️⃣ Batch Ingestion

**POST** `/events/batch`

* Accepts a list of events and returns:

  * Accepted events
  * Deduplicated events
  * Updated events
  * Rejected events with reasons

### 2️⃣ Machine Stats

**GET** `/stats?machineId=...&start=...&end=...`
Returns:

* Total number of valid events
* Total defects (ignoring `-1`)
* Average defects per hour
* Machine health status

**Health rule:**

* `< 2.0 defects/hour → Healthy`
* `≥ 2.0 defects/hour → Warning`

### 3️⃣ Top Defect Lines

**GET** `/stats/top-defect-lines?factoryId=...&from=...&to=...&limit=...`
Returns per production line:

* Total defects
* Event count
* Defects per 100 events (percentage)

This identifies lines with the **highest defect contribution**.

---

## ⚡ Performance Strategy

To handle **large batches efficiently**:

* Events processed in-memory before database persistence
* **Batch operations** instead of per-event inserts
* Deduplication checks on **indexed columns**
* Only required fields queried for statistics

✅ Can process **1000 events under 1 second** on a standard laptop (see `BENCHMARK.md`).

---

## 🧪 Testing

Tests cover:

* Duplicate event handling
* Update vs ignore logic
* Validation failures
* Future event rejection
* Unknown defect exclusion
* Time window boundaries
* Concurrent batch ingestion
* Statistics correctness

---

## ⚙️ Setup & Run Instructions

**Prerequisites:**

* Java 17+
* Maven
* Local database (H2/MySQL/PostgreSQL)

**Steps:**

```bash
git clone <repository-url>
cd factory-events-system
mvn clean install
mvn spring-boot:run
```

**Application URL:**

```
http://localhost:8080
```

---

## 🧠 Edge Cases & Assumptions

* `eventTime` is always used for queries
* `receivedTime` from client is ignored and overwritten
* Duplicate detection based strictly on `eventId`
* Partial batch failures do not block valid events
* Unknown defects (`-1`) excluded only from defect metrics

---

## 🔮 Future Improvements

* Streaming ingestion using **Kafka/RabbitMQ**
* Pre-aggregated statistics tables
* Pagination & caching for heavy queries
* Distributed locking for multi-instance deployment
* Monitoring & alerting

---

## 👨‍💻 Author

Developed as a **backend engineering assignment** to demonstrate:

* API design
* Concurrency handling
* Data consistency
* Performance optimization
* Real-world backend patterns
