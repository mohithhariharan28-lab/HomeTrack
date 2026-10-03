# HomeTrack - Household Asset Manager

## Aim

To provide a simple desktop application for recording household assets and keeping track of warranties, insurance, EMI payments, vehicle servicing, and home maintenance reminders.

## Features

- User registration and login with salted PBKDF2 password hashes.
- Add, view, update, delete, and search assets by name, category, or location.
- Track warranty provider and end date, insurance provider/end date/premium, and EMI lender/monthly amount/next due date.
- Set vehicle service or home maintenance reminder dates and notes.
- Dashboard totals for assets that are overdue, due within 30 days, or safe. An asset is counted once in the most urgent applicable category.
- Record service history with date, type, cost, and notes.
- Per-user asset records and cascading cleanup of related records.

## Technologies

- Java 17 or later
- Java Swing
- JDBC
- SQLite through Xerial SQLite JDBC
- `LocalDate` for reminder date calculations
- Maven for dependency management and execution

## Database Structure

The application creates `hometrack.db` in its current working directory when it starts. Tables are created automatically:

| Table | Purpose |
|---|---|
| `users` | Unique usernames and password hashes |
| `assets` | Asset owner, name, category, purchase details, location, and maintenance reminder |
| `warranties` | Warranty provider and end date, linked to an asset |
| `insurance` | Insurance provider, end date, and premium, linked to an asset |
| `emi` | Lender, monthly amount, and next due date, linked to an asset |
| `service_history` | Service date, service type, cost, and notes, linked to an asset |

Foreign keys are enabled for each connection. JDBC operations use `PreparedStatement` and `ResultSet`.

## How to Run

Install JDK 17+ and Maven, then open a terminal in this project directory:

```powershell
mvn clean compile
mvn exec:java
```

The first launch creates the database and opens the login screen. Register a username and password to begin.

Run the database-backed smoke test without opening the GUI or modifying `hometrack.db`:

```powershell
mvn -q compile exec:java -Dexec.args="--self-test"
```

The smoke test uses and removes a temporary SQLite database. It checks authentication, asset create/search/update/delete, tracking dates, dashboard calculations, and service history.

## Result

HomeTrack is a standalone Swing desktop application with SQLite persistence for household asset records and reminders. It is intended as a compact college mini project demonstrating classes and objects, encapsulation, inheritance, polymorphism, interfaces, and JDBC database operations.
