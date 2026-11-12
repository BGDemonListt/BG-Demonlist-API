# AGENTS.md

This file is the working agreement for Codex and other contributors in this repository. Read it before starting any task, then inspect the relevant code and current working tree before making changes. More specific `AGENTS.md` files, if added below this directory, override this file for their subtree.

## Project Overview

BG Demonlist API is a Java 21/Spring Boot 3.4 backend for the Bulgarian Geometry Dash Demonlist. It provides authentication, OAuth account linking, demon and skillset management, player rankings, record moderation, player claims, Geometry Dash metadata lookups, and static file delivery.

The application uses:

- Maven Wrapper (`./mvnw`) as the build and dependency-management entry point.
- PostgreSQL with Spring Data JPA/Hibernate.
- Flyway for schema migrations. Flyway is enabled and Hibernate uses `ddl-auto: validate`.
- Spring Security with JWT cookies, Google OAuth, and Discord account linking.
- JUnit 5, Mockito, Spring Boot Test, Testcontainers, and H2 for testing.
- Lombok and ModelMapper in the application code.

## Repository Structure

- `src/main/java/com/bgdl/bgdl/`
  - `controllers/`: HTTP endpoints. Most application endpoints are under `/api/v1`; external/static endpoints use `/gd/api` and `/files`.
  - `services/`: service contracts.
  - `services/impl/`: business logic, transaction boundaries, and background leaderboard work.
  - `repositories/`: Spring Data repositories and JPA specifications.
  - `models/entity/`: persisted JPA entities.
  - `models/request/`, `models/response/`, `models/dto/`: API and integration boundary models.
  - `exceptions/` and `handlers/`: domain/API exceptions and centralized exception handling.
  - `security/`: authentication filters, cookie handling, and logout behavior.
  - `config/`: application, security, web, mail, async, and rate-limiter configuration.
  - `enums/`, `constants/`, and `utils/`: shared supporting types and helpers.
- `src/main/resources/application.yaml`: runtime configuration and environment-variable bindings.
- `src/main/resources/rate-limiter.yaml`: Resilience4j rate-limit definitions.
- `src/main/resources/db/migration/`: ordered Flyway SQL migrations.
- `src/main/resources/static/`: static files served by the API.
- `src/test/java/com/bgdl/bgdl/`: unit and integration tests, mirroring the main package layout.
- `src/test/resources/`: test-only configuration and Mockito support.
- `docs/postman/BGDL-API.postman_collection.json`: maintained Postman collection for the public API.
- `docs/manual-testing.md`: manual end-to-end verification procedures.
- `README.md`: product behavior, setup, environment, and API overview.
- `HELP.md`: short contributor reference.

## Architecture And Change Boundaries

- Keep controllers thin: validate/bind HTTP input, enforce endpoint security, delegate to a service, and return the appropriate response/status.
- Put business rules in services, persistence queries in repositories/specifications, and representation mapping in the established service/helper layer.
- Use request and response models at the HTTP boundary; do not expose JPA entities directly.
- Reuse existing exception types and `CustomExceptionHandler` behavior. Add a focused domain exception when a new failure mode needs a distinct API response.
- Preserve soft-delete behavior (`deletedAt`) and active-record filters when changing entities or queries.
- Treat leaderboard recalculation and its after-commit/background execution as consistency-sensitive code. Add regression coverage for transaction timing, ranking, or record/demon changes.
- Keep security-sensitive behavior explicit. Do not log tokens, passwords, OAuth codes, secrets, or authentication cookies.

## Key Commands

Run commands from the repository root. Prefer the checked-in Maven Wrapper over a globally installed Maven version.

```bash
# Start the API on the configured port (8080 by default)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run one test class or one test method
./mvnw -Dtest=DemonServiceImplTest test
./mvnw -Dtest='DemonServiceImplTest#createDemonInsertsAtRequestedPositionAndRequestsLeaderboardRebuild' test

# Clean, test, and build the application artifact
./mvnw clean verify

# Build the package with the normal test phase
./mvnw clean package
```

PostgreSQL Testcontainers tests require a running Docker daemon. Tests annotated with `disabledWithoutDocker = true` may be skipped when Docker is unavailable; report that fact instead of presenting a skipped integration test as verified.

Local startup requires the environment variables documented in `README.md`, including database, JWT, frontend/backend URL, mail, and OAuth values. Never commit real credentials or local `.env` files.

## Java And Styling Conventions

- The repository's authoritative Java style check is Checkstyle through the CheckStyle-IDEA plugin configured in IntelliJ. Run it on all changed Java files before completing a task or opening a PR, and fix all applicable violations.
- Checkstyle is not currently bound to the Maven lifecycle, so a successful Maven build does not replace the IntelliJ Checkstyle check.
- Match the surrounding code: 4-space indentation, braces on the same line, one public top-level type per file, descriptive names, and small cohesive methods.
- Keep packages under `com.bgdl.bgdl` and place new types in the existing layer that owns the behavior.
- Keep imports explicit and organized; remove unused imports. Avoid wildcard imports except where the existing framework/annotation style requires them.
- Prefer constructor injection for new Spring collaborators. Follow an existing class's injection style when a focused change does not justify refactoring it.
- Use Lombok consistently with neighboring models, but do not hide important lifecycle, equality, or validation behavior behind generated code.
- Prefer existing helpers, response types, exceptions, and repository methods over duplicate logic.
- Keep comments scarce and useful: explain non-obvious constraints or concurrency/transaction reasoning, not what the code already states.
- Do not add broad exception catches, silent fallbacks, unchecked casts, or suppressions merely to pass compilation or Checkstyle.

## Testing Guidelines

- Every task must finish with a test-impact review. Add or update unit tests whenever behavior, validation, mapping, error handling, security, or a bug fix changes. Documentation-only or mechanically equivalent changes may not need a new test; state why in the handoff/PR.
- Place tests in the matching package under `src/test/java` and use the `*Test` suffix.
- Use JUnit 5 and Mockito (`@ExtendWith(MockitoExtension.class)`) for focused service tests. Test behavior and observable collaboration rather than private implementation details.
- Cover the successful path plus important validation, authorization, not-found, ordering, soft-delete, and failure paths introduced or changed by the task.
- Use integration tests when correctness depends on JPA mappings, PostgreSQL SQL/locking, Flyway, transactions, or after-commit/background behavior. Follow the existing Testcontainers patterns.
- Run the smallest relevant test during development, then run `./mvnw clean verify` before completion when feasible.
- Do not weaken, delete, or skip an existing test solely to make a change pass. Fix the implementation or explain a legitimate behavior update in the test name and assertions.

## Database Migrations

- Every persistence-schema change must include a Flyway migration in `src/main/resources/db/migration` when needed. This includes tables, columns, constraints, indexes, relationships, enum/check constraints, required data backfills, and changes that affect existing rows.
- Determine the highest existing migration version, then add the next file using `V<next>__<Concise_description>.sql` (the sequence ends at `V11` at the time of writing). Never reuse a version number.
- Do not edit an already-applied migration to evolve the schema; add a new migration. Only correct an old migration when the task explicitly establishes that it has never been applied outside disposable local databases.
- Keep the migration and JPA entity mapping in sync. Since Hibernate runs with `ddl-auto: validate`, startup must validate after Flyway applies the migration.
- Write migrations that are deterministic and safe for existing data. Add explicit backfills before making a new column non-null, and consider indexes for new query patterns.
- Test migration-sensitive changes against PostgreSQL/Testcontainers where practical, not only H2.

## Postman And API Documentation

- Every task must finish with an API-contract review. Update `docs/postman/BGDL-API.postman_collection.json` when needed for any changed or added endpoint, HTTP method, path, query parameter, header, cookie, authentication rule, request body, response shape, or expected status.
- Keep collection folders, variables, example payloads, descriptions, and test scripts consistent with the implemented behavior. Do not put real tokens, credentials, or user data in the collection.
- Update `docs/manual-testing.md` when the manual flow or required verification changes.
- Update `README.md` and/or `HELP.md` when setup, environment variables, commands, runtime behavior, or the public API overview changes.
- If an implementation change does not affect the API contract, explicitly mark the Postman update as not needed in the task handoff/PR.

## Required Task Completion Checklist

After every single task, Codex must review and complete all applicable items below. Do not assume they are out of scope merely because the primary implementation is finished.

- [ ] Implement the requested behavior completely and keep unrelated user changes intact.
- [ ] Add or update unit tests if the task changes testable behavior or fixes a defect.
- [ ] Add or update integration tests if persistence, transactions, security wiring, or cross-layer behavior requires them.
- [ ] Update the Postman collection if the API contract or authentication behavior changed.
- [ ] Add the next Flyway migration if the database schema or required persisted data changed.
- [ ] Update relevant documentation if setup, configuration, commands, or manual verification changed.
- [ ] Run targeted tests and, when feasible, `./mvnw clean verify`.
- [ ] Run IntelliJ Checkstyle on changed Java files and resolve applicable findings.
- [ ] Review `git diff`/`git status` for accidental files, secrets, generated output, and unrelated formatting.
- [ ] Report completed checks and clearly identify any skipped or unavailable verification.

## Pull Request Guidelines

- Keep each PR focused on one coherent change. Avoid unrelated refactors, mass formatting, generated files, or dependency upgrades.
- Use a short imperative title consistent with repository history, for example `Add player claim validation` or `Fix leaderboard rebuild ordering`.
- In the PR description, explain the problem, the chosen behavior, notable implementation decisions, and any compatibility or rollout concerns.
- Include verification performed: exact Maven test commands, Checkstyle status, and any manual/Postman checks. Call out Docker-dependent tests that were skipped.
- Include an explicit impact statement for all three maintenance surfaces: **Tests**, **Postman**, and **Migrations**. Describe the updates or write `Not needed` with a brief reason.
- Call out changes to endpoints, payloads, status codes, security/authorization, environment variables, rate limits, and database schema prominently.
- Preserve backward compatibility unless the task intentionally changes the contract. Document intentional breaking changes and migration/consumer steps.
- Ensure the branch builds, relevant tests pass, Checkstyle passes, migrations are ordered, and documentation agrees with the code before requesting review.
- Do not commit secrets, IDE-local state, build output (`target/`), logs, or machine-specific files.

Suggested PR checklist:

```markdown
- [ ] Relevant unit/integration tests added or updated
- [ ] `./mvnw clean verify` passes (or limitation documented)
- [ ] IntelliJ Checkstyle passes for changed Java files
- [ ] Postman collection updated, or marked not needed with a reason
- [ ] Flyway migration added, or marked not needed with a reason
- [ ] README/HELP/manual testing docs updated where applicable
- [ ] No secrets, generated output, or unrelated changes included
```
