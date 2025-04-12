# 🕒 Dynamic Cron Job Scheduler (Spring Boot)

This project is a **dynamic cron-based job scheduler**. It allows you to:

- Register and schedule jobs at runtime
- Store job metadata and state in a database
- Dynamically activate, deactivate, update, or delete jobs via REST APIs
- Prevent job overlap and track execution history

---

## 🚀 Features

- Schedule jobs based on **cron expressions** stored in a database
- Auto-discovery of job classes implementing a common interface
- Logs job start/stop to the console and/or database
- Hot rescheduling or deactivation without app restart
- RESTful endpoints to manage and monitor jobs

---

## 📦 Technologies Used

- **Spring Boot**
- **MySQL** (configurable DB)

---

## ▶️ How to Run

### 1. Clone the repository

```bash
git clone https://github.com/chuongtran01/scheduler
cd scheduler
```

### 2. Configure your application properties

In `application.yml`, configure your DB (e.g., H2, MySQL).

### 3. Run the application

```bash
./mvnw spring-boot:run
```

---

## 🔗 REST API Endpoints

| Method | Endpoint                 | Description                 |
|--------|--------------------------|-----------------------------|
| GET    | `/api/scheduler`         | List all jobs               |
| GET    | `/api/scheduler/{id}`    | Get a specific job by ID    |
| PATCH  | `/api/scheduler`         | Create or update a job      |
| DELETE | `/api/scheduler/{id}`    | Delete a job                |
| GET    | `/api/scheduler/active`  | Get active (scheduled) jobs |
| GET    | `/api/scheduler/running` | Get jobs currently running  |

---

## ✍️ Define Your Own Job

To define a new job:

```java

@Component
public class SampleJob implements RunnableJob {
    @Override
    public void run() {
        System.out.println("Executing SampleJob...");
    }
}
```

Then register it via the `/api/scheduler` endpoint with its class name (`SampleJob`) and a cron expression.

---

## ✅ Example Cron Expressions

- `"0 0 * * * *"` → Every hour
- `"*/5 * * * * *"` → Every 5 seconds (for testing)
- `"0 0 12 * * ?"` → Every day at noon

---

## 🛑 Shutdown Behavior

All scheduled jobs are automatically canceled when the application shuts down to avoid leaks or hanging threads.

---

## 📖 License

MIT License — use freely with attribution.

---

## 🙌 Contributing

Pull requests and contributions are welcome. Please include unit or integration tests where applicable.

---

## 👨‍💻 Author

Created by [Chuong Tran](https://github.com/chuongtran01)
