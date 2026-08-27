# StackIt

A single-user personal budget tracker desktop app. Register, create monthly budgets with a set income, log expenses against them by category, and see what you have left in real time.

This app was originally built for a class in college. It targets college students on a fixed monthly budget who want a simple, private, offline tool to see where their money goes - no cloud account and no internet.

## Stack

Java SE 21 · Swing · JDBC · MySQL 8/9 · JUnit 4

## Quick start

1. Run `schema.sql` in MySQL Workbench — creates the `stackit` database and its three tables (idempotent, safe to re-run).
2. Set your MySQL password in `resources/db.properties`.
3. Import the project into Eclipse and confirm the build path has JDK 21, JUnit 4, and the MySQL Connector/J JAR.
4. Run `DBTest.java` to verify the connection, then `App.java` to launch.

Log in with `demo` / `demo123` for a seeded account, or register a new one.

## How it's built

Layered MVC, one direction only — each layer talks only to the one below it:

```
view  →  controller  →  service  →  dao  →  MySQL
```

- **view** — Swing dialogs and windows. No SQL, no business logic.
- **controller** — thin pass-through from UI to services.
- **service** — all validation and business rules (over-budget guard, unique budget names, input checks).
- **dao** — persistence behind interfaces. JDBC implementations for production; in-memory fakes for tests. All queries are parameterized (`PreparedStatement`) and use try-with-resources.

Dependencies are wired once at startup in `App.java` and passed down via constructors. Because the service layer depends on DAO *interfaces*, the same services run against MySQL in production or in-memory fakes under test.

## Domain model

Two entities in a one-to-many relationship, plus a supporting `User` for auth:

- **Budget** — a budgeting period with a name, date range, income, and status (`ACTIVE` / `ARCHIVED`).
- **BudgetItem** — a single expense under a budget, tagged with one of eight categories.
- **User** — an account. Stores only a salted SHA-256 password hash; the plain password never reaches the database.

Deletes cascade at the DB level: removing a user removes their budgets, and removing a budget removes its items.

## Notable details

- **Allocated / remaining totals are computed in SQL**, not Java — a single `LEFT JOIN + SUM + GROUP BY` per query, so the numbers can't drift out of sync with the underlying items.
- **Every record is scoped to the logged-in user**; each query filters by `user_id`.
- **The UI stays responsive** — table loads run on a background thread via `SwingWorker`.
- **24 JUnit tests** cover the service layer's happy paths and business rules using the in-memory DAOs.

## Scope

This is a deliberately scoped MVP. Recurring expenses, spending analytics, in-app account management, and password reset were cut from the original proposal to keep the core flow clean. Categories and income are modeled as an enum and a column rather than separate tables.

---


