# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Quick Commands

### Building and Running
```bash
# Build the entire project
mvn clean install

# Build specific modules  
mvn clean install -pl agent
mvn clean install -pl web-app
mvn clean install -pl auth-server
mvn clean install -pl mcp-location
mvn clean install -pl mcp-nomnom

# Run the web application (main UI)
cd web-app && mvn spring-boot:run
# Or from root: mvn spring-boot:run -pl web-app

# Run the OAuth2 Authorization Server
cd auth-server && mvn spring-boot:run
# Or from root: mvn spring-boot:run -pl auth-server

# Run the Location MCP Server (SSE)
cd mcp-location && mvn spring-boot:run
# Or from root: mvn spring-boot:run -pl mcp-location

# Run the NomNom MCP Server (Food ordering)
cd mcp-nomnom && mvn spring-boot:run
# Or from root: mvn spring-boot:run -pl mcp-nomnom

# Run with JAR (after building)
java -jar web-app/target/web-app-1.0.0-SNAPSHOT.jar
java -jar auth-server/target/auth-server-1.0.0-SNAPSHOT.jar
java -jar mcp-location/target/mcp-location-1.0.0-SNAPSHOT.jar
java -jar mcp-nomnom/target/mcp-nomnom-1.0.0-SNAPSHOT.jar
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl agent
mvn test -pl web-app
mvn test -pl auth-server
mvn test -pl mcp-location

# Run integration tests (auth-server)
mvn -pl auth-server -Pintegration-test verify

# Run a specific test class
mvn test -Dtest=AgendaTest -pl agent
mvn test -Dtest=MeetingAgentTest -pl agent
```

### Development
```bash
# Clean and rebuild everything
mvn clean compile

# Skip tests when building
mvn clean install -DskipTests

# Run in development mode with hot reload (DevTools enabled)
cd web-app && mvn spring-boot:run
```

## Project Architecture

This is a **multi-module Spring Boot application** that integrates **Embabel AI agents** and **Spring AI MCP Servers** for intelligent meeting planning, authentication, location management, and food ordering.

### Module Structure
- **`common/`** - Shared library with common domain models (Agenda, etc.) used across modules
- **`agent/`** - Spring Boot library module containing Embabel AI agent logic for meetings, locations, and food
- **`web-app/`** - Spring Boot web application (main UI) with OAuth2 client integration
- **`auth-server/`** - OAuth2 Authorization Server (port 9000) providing JWT tokens for the ecosystem
- **`mcp-location/`** - Spring AI MCP Server (SSE transport) exposing location/room booking tools
- **`mcp-nomnom/`** - Embabel MCP Server for food ordering integration

### Technology Stack
- **Java 21** (required - uses modern Java features like records)
- **Spring Boot 3.5.5** with Spring MVC and Spring Security
- **Embabel Agent Framework 0.1.3** - AI agent platform with MCP server support
- **Spring AI 1.0.3** - AI integration framework with MCP Server capabilities
- **Spring Authorization Server** - OAuth2/OpenID Connect provider
- **Thymeleaf** templates with **Bootstrap 5.3.2** UI
- **Maven** multi-module build system

### Key Architectural Patterns

#### Embabel Agent Integration
The core AI functionality is implemented using **multiple Embabel agents**:
- **`MeetingAgent`** - Main meeting planning agent with `@Agent` annotation
- **`LocationAgent`** - Handles location and room selection via MCP client
- **`FoodAndDrinksAgent`** - Manages food ordering through NomNom MCP integration
- **`HandleOrderAgent`** (mcp-nomnom) - Processes food orders with `@Agent` annotation
- Uses `@Action` and `@AchievesGoal` annotations for AI-driven operations
- Integrates OpenAI GPT-4 mini via `context.ai().withLlm(OpenAiModels.GPT_41_MINI)`
- Tools are exposed via `@Tool` annotations on service methods

#### Spring AI MCP Server Architecture
- **`mcp-location`** - Spring AI MCP Server using SSE (Server-Sent Events) transport
- **`LocationService`** - Exposes `@Tool` methods for location operations
- Tools: `all-locations`, `check-room-availability`, `book-room`
- Secured with OAuth2 Resource Server (JWT validation)
- Web UI for documentation and testing at http://localhost:8081

#### OAuth2 Security Architecture
- **`auth-server`** - Standalone OAuth2 Authorization Server (port 9000)
- Issues JWT tokens with RS256 signing
- Supports Authorization Code flow, Client Credentials, and PKCE
- Demo users: `user`, `admin`, `planner` with role-based access
- OAuth2 clients: `meeting-planner-web`, `meeting-planner-sse`, `meeting-planner-public`
- Web app and MCP servers validate JWT tokens as resource servers

#### Domain Model
- **`Meeting`** - Core entity with validation annotations
- **`MeetingRequest`** - Input DTO for meeting creation
- **`Person`** with **`Agenda`** (shared via `common/`) - Handles availability checking and booking
- **`MeetingService`** - In-memory persistence (no database)
- **`Participants`** - Manages groups with `@Tool` methods for AI integration

#### Web Layer
- Spring MVC with `HomeController`, `LocationController`, `NomNomController`, `LogoutController`
- OAuth2 Client configuration with auto-redirect to auth-server
- Thymeleaf templates in `web-app/src/main/resources/templates/`
- Bootstrap 5.3.2 responsive UI with jQuery
- Form-based interaction with agents

## Important Implementation Details

### Agent Tools Architecture
#### Participant Management Tools (`Participants` class)
- `@Tool` annotated methods for AI agent interaction
- `checkAvailabilityFor()` - Checks participant availability for specific time ranges
- `bookMeetingForAll()` - Books meetings for all participants
- `availabilityForDay()` - Retrieves available time slots for a full day

#### Person Finder (`PersonFinder` service)
- `findByEmail()` - Locates people by email
- Repository pattern for in-memory participant management

#### Location Tools (via MCP Server)
- `LocationService` in `mcp-location` exposes room booking tools
- `all-locations` - Returns all meeting locations with capacities
- `check-room-availability` - Validates room availability by date/time/duration
- `book-room` - Books specific rooms at locations

#### Food Ordering Tools (via MCP Server)
- `MenuService` in `mcp-nomnom` with `@Tool` methods
- Product catalog management for food and drink items
- Order processing through `HandleOrderAgent`

### AI Agent Flow
1. Web form submits `MeetingRequest` to controller
2. Controller creates `AgentInvocation` and invokes the `MeetingAgent`
3. Agent uses AI with natural language prompts to:
   - Find participants using tools
   - Check availability across all participants
   - Find optimal meeting times
   - Book the meeting

### Configuration
- **Environment Variables**: 
  - OpenAI API key in `.env` file (required for agent operations)
  - OAuth2 settings via environment variables or Spring profiles
- **Application Config**: 
  - `web-app/src/main/resources/application.yml` - Web app with OAuth2 client settings
  - `auth-server/src/main/resources/application.yml` - Auth server on ports 9000/9001
  - `mcp-location/src/main/resources/application.yml` - MCP server on port 8081
  - `mcp-nomnom/src/main/resources/application.yml` - NomNom MCP server configuration
- **Agent Config**: 
  - Embabel repositories configured in parent `pom.xml` (version 0.1.3)
  - Spring AI BOM for MCP server dependencies (version 1.0.3)

### Development Setup Notes
- Requires **Java 21** (specified in `.java-version`)
- Spring DevTools enabled for hot reloading during development
- Template caching disabled in development
- Uses webjars for frontend dependencies (Bootstrap, jQuery)

### Testing Approach
- JUnit 5 with comprehensive test coverage for domain logic
- Focuses on `Agenda` availability checking algorithm
- Uses `@DisplayName` for readable test descriptions
- Mock implementations for Embabel agent testing

## Local Development URLs

### Web Application (port 8080)
- **Home**: http://localhost:8080
- **Create Meeting**: http://localhost:8080/create-meeting  
- **View Meetings**: http://localhost:8080/meetings
- **Manage Persons**: http://localhost:8080/persons
- **Location Management**: http://localhost:8080/locations
- **Food Ordering**: http://localhost:8080/nomnom

### Auth Server (port 9000)
- **Authorization Endpoint**: http://localhost:9000/oauth2/authorize
- **Token Endpoint**: http://localhost:9000/oauth2/token
- **JWK Set**: http://localhost:9000/oauth2/jwks
- **OpenID Configuration**: http://localhost:9000/.well-known/openid-configuration
- **Login Page**: http://localhost:9000/login
- **Health Check**: http://localhost:9001/actuator/health

### MCP Location Server (port 8081)
- **Documentation**: http://localhost:8081
- **SSE Endpoint**: http://localhost:8081/mcp/sse (requires OAuth2 token)
- **Test Tools**: http://localhost:8081/test

## HTTP Testing
- **Root**: `test_requests.http` - Basic meeting planner API endpoints
- **Auth Server**: `auth-server/test-requests.http` - OAuth2 flow testing
- **Auth Server Script**: `test_oauth_mcp.sh` - Automated OAuth2 flow testing script

## Security & Authentication Notes

### OAuth2 Flow
1. User accesses web-app (http://localhost:8080)
2. Web-app redirects to auth-server for authentication
3. User logs in with demo credentials (e.g., `user`/`password`)
4. Auth-server issues JWT access token and refresh token
5. Web-app uses token to access protected resources
6. MCP servers validate JWT tokens as OAuth2 Resource Servers

### Demo OAuth2 Clients
- **meeting-planner-web**: Web app client (Authorization Code flow)
- **meeting-planner-sse**: SSE/MCP client (Client Credentials flow)
- **meeting-planner-public**: Public client (PKCE for mobile/SPA)

### Running the Full Stack
```bash
# Terminal 1 - Auth Server (must start first)
mvn spring-boot:run -pl auth-server

# Terminal 2 - Location MCP Server
mvn spring-boot:run -pl mcp-location

# Terminal 3 - NomNom MCP Server
mvn spring-boot:run -pl mcp-nomnom

# Terminal 4 - Web Application
mvn spring-boot:run -pl web-app
```

## Additional Documentation
- **AGENTS.md** - Repository guidelines, coding standards, testing patterns
- **README.md** - Comprehensive project documentation with architecture details
- **auth-server/README.md** - OAuth2 setup, client configuration, security features
