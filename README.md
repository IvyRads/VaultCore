# VaultCore Banking Management System

Spring Boot REST backend for the existing Supabase PostgreSQL schema. The backend does not create a schema and does not use Supabase API keys; it connects with PostgreSQL credentials.

## Requirements

- Java 25 LTS or later
- Maven 3.9 or later
- Existing Supabase PostgreSQL tables
- Frontend served by Live Server or another local HTTP server

## Start

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `FRONTEND_ORIGIN` as described in [SETUP.md](SETUP.md), then run:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

The API is at `http://localhost:8080`. The frontend must call the API, never the database directly. See [API_MAPPING.md](API_MAPPING.md).

## Security

Passwords are BCrypt-hashed. JWTs are stateless and the signing secret remains on the backend. Registration temporarily marks users verified because email verification was explicitly excluded. No employee role, OTP, email service, or direct balance-edit operation is included.

## Schema

The confirmed column inventory was used for entity mapping. Before first start, provide the complete nullability/default/foreign-key export if those constraints differ from the supplied inventory. `spring.jpa.hibernate.ddl-auto=validate` deliberately fails startup on incompatible schema.
