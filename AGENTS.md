# Repository Guidelines

## Project Structure & Module Organization
- Root `pom.xml` manages the Maven multi-module build across Spring Boot services.
- `common` holds shared domain models (Agenda, etc.) consumed by every module.
- `agent` packages the Embabel-powered meeting agent with AI-driven meeting planning, location, and food ordering capabilities.
- `mcp-location` is a Spring AI MCP Server (SSE transport) exposing location and room booking tools on port 8081.
- `mcp-nomnom` is an Embabel MCP Server handling food ordering integration.
- `auth-server` provides the OAuth2 Authorization Server (port 9000), while `web-app` delivers the UI (templates plus static assets) on port 8080.
- Each module keeps code under `src/main/java` with matching tests in `src/test/java`.

## Build, Test, and Development Commands
- `mvn clean install` (root): compile all modules, run unit tests, and publish JARs to the local repository.
- `mvn -pl agent spring-boot:run`: launch the Embabel agent with hot reload.
- `mvn -pl web-app spring-boot:run`: start the user-facing application (port 8080).
- `mvn -pl auth-server spring-boot:run`: run the OAuth2 Authorization Server (port 9000).
- `mvn -pl mcp-location spring-boot:run`: start the Spring AI MCP Server for location services (port 8081).
- `mvn -pl mcp-nomnom spring-boot:run`: start the Embabel MCP Server for food ordering.
- `mvn -pl auth-server -Pintegration-test verify`: execute Failsafe-driven integration suites (e.g., `AuthorizationCodeFlowIntegrationTest`).

## Coding Style & Naming Conventions
- Target Java 21 with 4-space indentation; keep line length near 120 characters for readability.
- Use package naming `org.rag4j.meeting.<module>` (note: groupId is `org.rag4j.meeting`) and name services/controllers with explicit suffixes (e.g., `MeetingService`, `AuthServerApplication`).
- Favor constructor injection, records for immutable DTOs, and keep configuration in `application.yml` per module.
- Use `@Agent` and `@Action` annotations for Embabel agent implementations; `@Tool` annotations for AI-accessible service methods.
- Before submitting, run `mvn fmt:format` if you add the Maven Formatter plugin locally, otherwise ensure your IDE applies standard Java formatting.

## Testing Guidelines
- Unit tests use JUnit Jupiter and Mockito; name them `<Subject>Test.java` alongside the production class.
- Place scenario-heavy tests (agent orchestration, OAuth flows) in integration suites ending with `*IntegrationTest` and run them via the `integration-test` profile.
- Aim to cover public service methods and edge cases (null handling, OAuth error responses); reference existing tests under `agent/src/test/java` for patterns.

## Commit & Pull Request Guidelines
- Follow the repository’s history: short, capitalized, present-tense messages (e.g., “Add meeting agent service wiring”).
- Each PR should link relevant issues, describe behavioral changes, and note manual verification (scripts like `test_oauth_mcp.sh` are good references).
- Include screenshots for UI tweaks and list affected modules when changes span multiple services.

## Security & Configuration Tips
- Store secrets (OpenAI API keys, etc.) in environment variables or `.env` files, not in `application.yml`; `.env` files are Git-ignored.
- OAuth2 flow: web-app and MCP servers act as resource servers validating JWT tokens issued by auth-server.
- Keep TLS and OAuth client settings synchronized between `auth-server` and clients; update both whenever redirect URIs or cookie names change.
- MCP servers use OAuth2 Resource Server configuration with JWT validation for secure tool access.

## Framework & Dependency Versions
- **Java**: 21 (required)
- **Spring Boot**: 3.5.5
- **Spring AI**: 1.0.3 (for MCP Server support)
- **Embabel Agent Framework**: 0.2.0
- **Bootstrap**: 5.3.8 (webjars)
- **jQuery**: 3.7.1 (webjars)
