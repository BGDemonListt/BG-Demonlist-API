# BG Demonlist API

Backend API for the Bulgarian Geometry Dash Demonlist platform. The service manages authentication, demon catalog data, leaderboard players, record submissions, skillset tagging, Geometry Dash level lookups, and static file delivery for region assets.

## What is in the API

- JWT authentication with registration, email confirmation, login, refresh-token rotation, and password reset
- Google OAuth sign-in plus Discord account linking for existing users
- Paginated demon list with search and skillset-tag filtering
- Player leaderboard with 1-based pagination, region filtering, hardest demon tracking, and completed-demon history
- Record submission moderation that triggers leaderboard rebuilds whenever accepted runs change
- Admin CRUD for demons, users, and skillset tags
- Static file serving for region flags and uploaded assets under `/files/**`
- Geometry Dash level metadata proxy under `/gd/api/{levelId}`

## Tech stack

- Java 21
- Spring Boot 3.4
- Spring Security + JWT
- PostgreSQL
- JPA / Hibernate
- Resilience4j rate limiting
- Google OAuth + Discord OAuth linking
- Spring Mail

## Current behavior worth knowing

- Player pages return up to `15` items per page.
- Demon pages return up to `20` items per page.
- Page numbers are 1-based.
- `POST /api/v1/records` always stores submissions as `PENDING`, even if another status is sent.
- Demon order changes and accepted-record changes both request a leaderboard rebuild.
- Flyway scripts are present in `src/main/resources/db/migration`, but Flyway is currently disabled in `src/main/resources/application.yaml`; schema changes are applied through Hibernate `ddl-auto=update`.
- General endpoints are rate-limited to `100` requests per `30m`; sensitive endpoints are limited to `10` requests per `1m`.

## Environment configuration

The application reads its runtime settings from environment variables referenced in `src/main/resources/application.yaml`.

### Required

- `DB_URL` example: `postgresql://localhost:5432/bgdl`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `REFRESH_TOKEN_EXPIRATION`
- `FRONTEND_URL`
- `BACKEND_URL`
- `GMAIL_USERNAME`
- `GMAIL_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

### Needed for Discord linking

- `DISCORD_CLIENT_ID`
- `DISCORD_CLIENT_SECRET`

### Frontend redirects derived from `FRONTEND_URL`

- `${FRONTEND_URL}/login`
- `${FRONTEND_URL}/process-oauth2`
- `${FRONTEND_URL}/process-discord-link`
- `${FRONTEND_URL}/forgotten-password`

## Running locally

1. Create a PostgreSQL database.
2. Export the environment variables listed above.
3. Start the API:

```bash
./mvnw spring-boot:run
```

4. Run the test suite when needed:

```bash
./mvnw test
```

The API starts on `http://localhost:8080` by default.

## API overview

### Authentication

- `POST /api/v1/auth/register`
- `GET /api/v1/auth/registrationConfirm?token=...`
- `POST /api/v1/auth/authenticate`
- `POST /api/v1/auth/refresh-token`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/forgot-password?email=...`
- `POST /api/v1/auth/password-reset?token=...&newPassword=...`

Authentication endpoints set `HttpOnly` auth cookies for the access token and refresh token. Authenticated browser requests should be sent with credentials enabled so the API receives cookies instead of an `Authorization` bearer header.

### OAuth2 and account linking

- `GET /api/v1/oauth2/url/google`
- `GET /api/v1/oauth2/authenticate/google?code=...`
- `GET /api/v1/oauth2/url/discord/link`
- `POST /api/v1/oauth2/link/discord?code=...&state=...`
- `DELETE /api/v1/oauth2/link/discord`

### Demons

- `GET /api/v1/demons?page=1&name=&tagIds=<uuid>`
- `GET /api/v1/demons/{levelId}`
- `POST /api/v1/demons`
- `PUT /api/v1/demons/{levelId}`
- `DELETE /api/v1/demons/{id}`

Important demon features:

- search by partial `name`
- filter by one or more `tagIds`
- `youtubeUrl` is supported on demons
- up to `4` skillset tags can be attached to a demon
- points are recalculated from list position

### Players

- `GET /api/v1/players?page=1&name=&region=SOFIA`
- `GET /api/v1/players/regions`
- `GET /api/v1/players/{id}`

Player details include:

- region metadata
- leaderboard rank
- hardest completed demon
- completed demons sorted by demon position

### Record submissions

- `GET /api/v1/records`
- `POST /api/v1/records`
- `PUT /api/v1/records`
- `DELETE /api/v1/records/{id}`

Submission payloads support:

- `progress`
- `youtubeUrl`
- `rawFootageUrl`
- `description`
- `playerId`
- `demonId`

### Users

- `GET /api/v1/users/all`
- `GET /api/v1/users/{id}/admin`
- `PUT /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/profile`
- `DELETE /api/v1/users/{id}`

Profile updates currently support:

- `name`
- `region`
- Discord profile data is returned as read-only response data

### Skillset tags

- `GET /api/v1/skillset-tags`
- `POST /api/v1/skillset-tags`
- `PUT /api/v1/skillset-tags/{id}`
- `DELETE /api/v1/skillset-tags/{id}`

### External and static resources

- `GET /gd/api/{levelId}` fetches Geometry Dash level data
- `GET /files/{*filePath}` serves static assets with cache headers

Example file request:

```text
/files/regions/flags/targovishte.png
```

## Example payloads

### Create a demon

```json
{
  "levelTitle": "Example Demon",
  "levelId": 123456,
  "creatorName": "Creator",
  "creatorId": 9999,
  "description": "Short description",
  "levelPassword": "Free Copy",
  "youtubeUrl": "https://www.youtube.com/watch?v=example",
  "musicName": "Track Name",
  "musicId": 1234,
  "musicCreatorName": "Artist",
  "musicUrl": "https://www.newgrounds.com/audio/listen/example",
  "requirement": 100,
  "position": 1,
  "difficulty": "EXTREME",
  "skillsetTagIds": []
}
```

### Update a profile

```json
{
  "name": "Player Name",
  "region": "SOFIA"
}
```

### Create a record submission

```json
{
  "playerId": "00000000-0000-0000-0000-000000000000",
  "demonId": "11111111-1111-1111-1111-111111111111",
  "progress": 100,
  "youtubeUrl": "https://www.youtube.com/watch?v=example",
  "rawFootageUrl": "https://drive.google.com/example",
  "description": "Click sync run"
}
```

## Docs in this repo

- Main project guide: `README.md`
- Developer quick reference: `HELP.md`
- Manual QA checklist: `docs/manual-testing.md`
- Postman collection: `docs/postman/BGDL-API.postman_collection.json`
