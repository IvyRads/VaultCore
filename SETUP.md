# Setup

## VALUES YOU MUST REPLACE

1. `DB_URL`: the Supabase PostgreSQL JDBC URL from Dashboard -> Connect -> PostgreSQL. Use the server-side connection string and append `?sslmode=require` when needed.
2. `DB_USERNAME`: the PostgreSQL database username, not the Supabase anon key.
3. `DB_PASSWORD`: the PostgreSQL database password, not an API key.
4. `JWT_SECRET`: a private random value of at least 32 characters. This is created for this backend and is not a Supabase key.
5. `FRONTEND_ORIGIN`: the URL serving the HTML, currently `http://127.0.0.1:5500`.

Copy `backend/.env.example` to a local environment configuration and set these values. Never place them in HTML, JavaScript, or source control.

## Run

Requirements: Java 25 LTS or later, Maven 3.9+, and a Supabase database with the existing tables. From the repository root:

```powershell
$env:DB_URL="jdbc:postgresql://HOST:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_DATABASE_PASSWORD"
$env:JWT_SECRET="a-long-private-random-secret-at-least-32-chars"
$env:FRONTEND_ORIGIN="http://127.0.0.1:5500"
mvn -f backend/pom.xml spring-boot:run
```

The server is `http://localhost:8080`. Hibernate uses `ddl-auto=validate`; startup will fail rather than create or replace tables when the schema does not match.
