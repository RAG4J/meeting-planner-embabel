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

# Run the web application
cd web-app && mvn spring-boot:run
# Or from root: mvn spring-boot:run -pl web-app

# Run with JAR (after building)
java -jar web-app/target/web-app-1.0.0-SNAPSHOT.jar
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl agent
mvn test -pl web-app

# Run a specific test class
mvn test -Dtest=AgendaTest -pl agent
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

This is a **multi-module Spring Boot application** that integrates **Embabel AI agents** for intelligent meeting planning.

### Module Structure
- **`agent/`** - Spring Boot library module containing Embabel AI agent logic
- **`web-app/`** - Spring Boot web application that uses the agent module

### Technology Stack
- **Java 21** (required - uses modern Java features)
- **Spring Boot 3.5.5** with Spring MVC
- **Embabel Agent Framework 0.1.1** - AI agent platform
- **Thymeleaf** templates with **Bootstrap 5.3.2** UI
- **Maven** multi-module build system

### Key Architectural Patterns

#### Embabel Agent Integration
The core AI functionality is implemented using **Embabel agents**:
- `MeetingAgent` is the main agent class with `@Agent` annotation
- Uses `@Action` and `@AchievesGoal` annotations for AI-driven operations
- Integrates OpenAI GPT-4 mini via `context.ai().withLlm(OpenAiModels.GPT_41_MINI)`
- Tools are exposed via `@Tool` annotations on service methods

#### Domain Model
- **`Meeting`** - Core entity with validation annotations
- **`MeetingRequest`** - Input DTO for meeting creation
- **`Person`** with **`Agenda`** - Handles availability checking and booking
- **`MeetingService`** - In-memory persistence (no database)

#### Web Layer
- Spring MVC with `HomeController`
- Thymeleaf templates in `web-app/src/main/resources/templates/`
- Bootstrap-based responsive UI
- Form-based interaction with the agent

## Important Implementation Details

### Agent Tools Architecture
The `PersonFinder` service exposes several `@Tool` methods that the AI agent can use:
- `findByEmail()` - Locates people by email
- `checkPersonAvailability()` - Checks if someone is available
- `bookMeetingForPerson()` - Books time slots
- `getPersonAvailabilityForDay()` - Gets available time slots

### AI Agent Flow
1. Web form submits `MeetingRequest` to controller
2. Controller creates `AgentInvocation` and invokes the `MeetingAgent`
3. Agent uses AI with natural language prompts to:
   - Find participants using tools
   - Check availability across all participants
   - Find optimal meeting times
   - Book the meeting

### Configuration
- **Environment Variables**: OpenAI API key in `.env` file
- **Application Config**: `web-app/src/main/resources/application.yml`
- **Agent Config**: Embabel repositories configured in parent `pom.xml`

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
- **Application**: http://localhost:8080
- **Create Meeting**: http://localhost:8080/create-meeting  
- **View Meetings**: http://localhost:8080/meetings

## HTTP Testing
Use the included `test_requests.http` file for testing API endpoints during development.
