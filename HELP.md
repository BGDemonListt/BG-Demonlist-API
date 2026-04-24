# BGDL API Help

Quick reference for contributors working on the local API.

## Useful commands

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw clean package
```

## Runtime checklist

- Java `21`
- PostgreSQL database reachable through `DB_URL`, `DB_USER`, and `DB_PASSWORD`
- JWT config: `JWT_SECRET`, `JWT_EXPIRATION`, `REFRESH_TOKEN_EXPIRATION`
- Frontend URLs driven by `FRONTEND_URL`
- Mail config: `GMAIL_USERNAME`, `GMAIL_PASSWORD`
- Google OAuth login: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- Discord account linking: `DISCORD_CLIENT_ID`, `DISCORD_CLIENT_SECRET`

## API behavior cheatsheet

- Base URL: `http://localhost:8080`
- Player pagination size: `15`
- Demon pagination size: `20`
- Player and demon pages are 1-based
- Record creation always forces `PENDING`
- Region assets are served through `/files/regions/flags/*.png`
- Demon list filters: `name`, `tagIds`
- Player list filters: `name`, `region`

## Security and moderation notes

- JWT auth powers the local login flow.
- Google OAuth creates or reuses a user, enables it, and issues auth tokens.
- Discord OAuth is used only for linking/unlinking an existing authenticated account.
- Admin-only moderation covers demon CRUD, record review, and skillset-tag management through method security.

## Data and schema notes

- The project is configured for PostgreSQL.
- Flyway migrations exist in `src/main/resources/db/migration`.
- Flyway is disabled in the current app config, so Hibernate `ddl-auto=update` is doing schema updates locally.
- Static uploads are expected under `server/src/main/resources/static/uploads/`.

## Rate limits

- `general_api_rate_limiter`: `100` requests per `30m`
- `sensitive_operations_rate_limiter`: `10` requests per `1m`

## Repo docs

- Project overview: `README.md`
- Manual verification flow: `docs/manual-testing.md`
- Postman collection: `docs/postman/BGDL-API.postman_collection.json`
