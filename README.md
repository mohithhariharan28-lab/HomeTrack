# HomeTrack - Household Asset Manager

HomeTrack is a Java Swing desktop application for managing household assets, warranties, insurance, EMI payments, vehicle servicing, and home maintenance reminders.

## Features

* User registration and login with PBKDF2 password hashing
* Add, view, update, delete, and search assets
* Warranty and insurance tracking
* EMI payment tracking
* Vehicle and home maintenance reminders
* Service history
* Dashboard for overdue, due-soon, and safe assets
* SQLite database with per-user records

## Technologies

* Java 17+
* Java Swing
* JDBC
* SQLite
* Maven

## Database

The application automatically creates:

```text
hometrack.db
```

Main tables:

```text
users
assets
warranties
insurance
emi
service_history
```

## Run

### Using Maven

```powershell
mvn clean compile
mvn exec:java
```

### Without Maven

If `sqlite-jdbc.jar` is available:

```powershell
javac -cp "sqlite-jdbc.jar" -d "out" src\main\java\hometrack\*.java
java -cp "out;sqlite-jdbc.jar" hometrack.Main
```

## Self-Test

```powershell
mvn -q compile exec:java -Dexec.args="--self-test"
```

## Result

HomeTrack is a standalone desktop application demonstrating Java OOP, Swing GUI, JDBC, SQLite, authentication, and CRUD database operations.
