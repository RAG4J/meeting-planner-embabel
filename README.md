# Meeting Planner Embabel

A sophisticated AI-powered meeting planning application built with **Embabel AI agents**, **Spring Boot**, and **Maven**. This application demonstrates intelligent meeting scheduling using real AI capabilities for participant availability checking, optimal time finding, and automated meeting booking.

## Project Structure

This is a multi-module Maven project leveraging the **Embabel Agent Framework 0.1.1** for AI-driven meeting planning:

```
meeting-planner-embabel/
├── pom.xml                 # Parent POM with Embabel repositories
├── web-app/                # Spring Boot web application
│   ├── src/main/java/      # Web controllers and configuration
│   ├── src/main/resources/ # Thymeleaf templates, Bootstrap UI, YAML config
│   └── pom.xml            # Web app dependencies
├── agent/                  # Embabel AI agent library
│   ├── src/main/java/      # AI agent logic with @Agent, @Action, @Tool annotations
│   ├── src/test/java/      # Comprehensive test suite
│   └── pom.xml            # Agent dependencies with Embabel starter
├── .env                    # OpenAI API configuration
├── WARP.md                # Development guidelines for Warp terminal
├── test_requests.http      # HTTP testing endpoints
└── README.md              # This file
```

## Technology Stack

- **Java 21** - Modern Java features including records and pattern matching
- **Spring Boot 3.5.5** - Application framework with auto-configuration
- **Embabel Agent Framework 0.1.1** - AI agent platform for intelligent automation
- **OpenAI GPT-4 mini** - Large language model for natural language processing
- **Thymeleaf** - Server-side template engine
- **Bootstrap 5.3.2** - Responsive CSS framework
- **Maven** - Multi-module build system
- **JUnit 5** - Testing framework with comprehensive coverage

## Modules

### Web App Module (`web-app/`)
- **Type**: Spring Boot executable application
- **Port**: 8080 (configurable)
- **Key Classes**:
  - `HomeController` - Main web controller with agent integration
  - `WebAppApplication` - Spring Boot main application class
- **Templates**: Complete Thymeleaf UI with Bootstrap styling
- **Dependencies**: Depends on `agent` module for AI functionality

### Agent Module (`agent/`)
- **Type**: Spring Boot library (JAR)
- **Purpose**: Embabel AI agent implementation for meeting intelligence
- **Key Classes**:
  - `MeetingAgent` - Core AI agent with `@Agent` annotation
  - `Participants` - AI tool provider with `@Tool` methods
  - `PersonFinder` - Repository for participant management
  - `MeetingService` - Meeting persistence and management
- **AI Capabilities**: Real-time availability checking, optimal scheduling, automated booking

## Prerequisites

- **Java 21** (required - uses modern Java features like records)
- **Maven 3.8** or higher  
- **OpenAI API Key** - Required for AI agent functionality

## Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd meeting-planner-embabel
```

### 2. Configure OpenAI API Key
Create a `.env` file in the project root:
```bash
OPENAI_API_KEY=your-openai-api-key-here
```

### 3. Verify Java Version
Ensure you're using Java 21:
```bash
java -version  # Should show Java 21
```

## Building the Project

### Build all modules
```bash
cd meeting-planner-embabel
mvn clean install
```

### Build specific module
```bash
# Build only the agent library
mvn clean install -pl agent

# Build only the web app
mvn clean install -pl web-app
```

## Running the Application

### Run the web application
```bash
cd meeting-planner-embabel/web-app
mvn spring-boot:run
```

Or run the compiled JAR:
```bash
java -jar web-app/target/web-app-1.0.0-SNAPSHOT.jar
```

The application will be available at: http://localhost:8080

## Features

### ✅ Implemented Features
- **AI-Powered Meeting Planning**: Real Embabel AI agent integration with OpenAI GPT-4 mini
- **Intelligent Participant Management**: Automatic availability checking across multiple participants
- **Smart Scheduling**: AI finds optimal meeting times when all participants are available
- **Automated Booking**: AI agent books meetings automatically after finding suitable times
- **Modern Web Interface**: Bootstrap 5.3.2 responsive UI with Thymeleaf templates
- **Comprehensive Person Management**: View individual agendas and availability
- **Real-time Availability Checking**: Live availability validation for meeting scheduling
- **In-Memory Data Persistence**: Quick development setup with sample participants

### 🔄 Future Enhancements
- Calendar system integration (Google Calendar, Outlook, etc.)
- Email notifications and reminders
- Meeting analytics and insights dashboard
- Advanced AI agenda generation
- Conflict resolution and alternative time suggestions
- Multi-timezone support

## Architecture

### Embabel Agent Integration
This application showcases a **production-ready Embabel AI agent** implementation:

#### Core AI Agent (`MeetingAgent`)
- **`@Agent`** annotation defines the AI agent with name, description, and version
- **`@Action`** methods for discrete AI-driven operations:
  - `findParticipants()` - Locates people by email addresses
  - `bookMeeting()` - AI-powered meeting scheduling with natural language processing
- **`@AchievesGoal`** annotation marks the main goal-achieving method
- **OpenAI GPT-4 mini integration** via `context.ai().withLlm(OpenAiModels.GPT_41_MINI)`
- **Tool-based AI interaction** using `@Tool` annotations on participant methods

#### AI Tools Architecture
The `Participants` class exposes AI tools via `@Tool` annotations:
- `checkAvailabilityFor()` - Checks participant availability for specific time ranges
- `bookMeetingForAll()` - Books meetings for all participants
- `availabilityForDay()` - Retrieves available time slots for a full day

#### Domain Model
- **`Meeting`** - Core entity with validation, status tracking, and time calculations
- **`MeetingRequest`** - Input DTO with comprehensive meeting parameters
- **`Person`** with **`Agenda`** - Handles individual availability and booking
- **`Participants`** - Manages groups of people with AI tool integration
- **`MeetingService`** - In-memory persistence for rapid development

### Web Application Architecture
Built on **Spring Boot 3.5.5** with modern web patterns:

#### Controllers
- **`HomeController`** - Handles all web routes and agent invocation
- **Agent Integration** - Direct `AgentInvocation` for AI-powered meeting creation
- **Validation** - Jakarta validation with comprehensive error handling
- **Responsive Design** - Bootstrap-based UI with Thymeleaf server-side rendering

#### Templates & UI
- **Thymeleaf Templates**: `index.html`, `create-meeting.html`, `meetings.html`, `persons.html`, `person-agenda.html`
- **Bootstrap 5.3.2**: Modern responsive design with minimal custom CSS
- **Form Handling**: Rich meeting creation form with participant management
- **Real-time Feedback**: Success/error messaging for AI operations

## Configuration

### Environment Variables
The application requires an OpenAI API key configured in `.env`:
```bash
OPENAI_API_KEY=your-openai-api-key-here
```

### Application Configuration
The main configuration is in `web-app/src/main/resources/application.yml`:

```yaml
server:
  port: 8080
  
spring:
  application:
    name: meeting-planner-web-app
  thymeleaf:
    cache: false          # Disabled for development
  devtools:
    restart:
      enabled: true       # Hot reloading enabled
    livereload:
      enabled: true

embabel:
  agent:
    timeout: 30s          # AI operation timeout
    
logging:
  level:
    com.embabel: INFO     # Embabel framework logging
    org.springframework.web: INFO
```

## Usage

### Web Interface
1. **Home Page**: http://localhost:8080 - Welcome page with navigation
2. **Create Meeting**: http://localhost:8080/create-meeting - AI-powered meeting creation form
3. **View Meetings**: http://localhost:8080/meetings - List all scheduled meetings
4. **Manage Persons**: http://localhost:8080/persons - View all participants
5. **Individual Agendas**: http://localhost:8080/persons/{email}/agenda - Personal calendar views

### Sample Participants
The application comes with pre-configured sample participants:
- `jettro@rag4j.org` - Jettro Coenradie
- `daniel@rag4j.org` - Daniël Spee  
- `joey@rag4j.org` - Joey Visbeen

### AI Agent Flow
1. User submits meeting request via web form
2. `HomeController` creates `AgentInvocation` and invokes `MeetingAgent`
3. AI agent uses natural language processing to:
   - Find participants using tool methods
   - Check availability across all participants
   - Find optimal meeting times
   - Automatically book the meeting
4. Results are displayed with success/error feedback

## Development

### Quick Development Commands
```bash
# Build and run
mvn clean install && mvn spring-boot:run -pl web-app

# Run tests
mvn test

# Run specific test
mvn test -Dtest=AgendaTest -pl agent

# Skip tests during build
mvn clean install -DskipTests
```

### Adding New Features
1. **New AI Tools**: Add `@Tool` methods to relevant model classes
2. **Agent Actions**: Extend `MeetingAgent` with new `@Action` methods
3. **Web Interface**: Update `HomeController` and Thymeleaf templates
4. **Domain Model**: Extend entities with new validation and business logic

### Testing Strategy
- **Unit Tests**: Comprehensive coverage for domain logic (especially `Agenda` availability algorithm)
- **Agent Tests**: Mock implementations using `embabel-agent-test` framework
- **Integration Tests**: End-to-end web interface testing
- **Test Naming**: Uses `@DisplayName` for readable test descriptions

### Code Style Guidelines
- **Java 21 Features**: Records, pattern matching, modern syntax encouraged
- **Spring Boot Conventions**: Constructor injection, auto-configuration patterns
- **Immutable Objects**: Prefer records and immutable designs
- **Validation**: Use Jakarta validation annotations throughout
- **Logging**: SLF4J with structured logging patterns

## License

[Add your license information here]

## Contributing

[Add contributing guidelines here]

## Support

[Add support information here]
