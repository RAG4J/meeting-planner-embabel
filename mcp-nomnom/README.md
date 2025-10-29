# NomNom Food Service - MCP Server

AI-powered food ordering service for meetings using Embabel MCP Server.

## Overview

The NomNom Food Service is an Embabel MCP Server that provides intelligent food ordering capabilities for meetings. It features AI-powered product matching and order processing through the HandleOrderAgent.

## Web Interface

The server now includes a Bootstrap 5-based web interface for browsing the menu and documentation.

### Features

- **Home Page** (`/`) - Server documentation and overview
- **Full Menu** (`/menu`) - Complete product catalog organized by category
- **Category Views** (`/category/{categoryName}`) - Detailed view of products in a specific category
- **AI-Powered Product Matching** - Intelligent product search with fuzzy matching

### Technology Stack

- **Embabel Agent Framework 0.1.3** - MCP server and agent capabilities
- **Spring Boot 3.5.5** - Application framework
- **Thymeleaf** - Template engine
- **Bootstrap 5.3.2** - Responsive UI framework
- **Bootstrap Icons** - Icon library
- **WebJars** - Front-end dependency management

## Running the Server

```bash
# From project root
mvn spring-boot:run -pl mcp-nomnom

# Or from the mcp-nomnom directory
cd mcp-nomnom
mvn spring-boot:run

# Or run the JAR
java -jar target/mcp-nomnom-1.0.0-SNAPSHOT.jar
```

The server will start on **port 8085** (configurable in `application.yml`).

## Accessing the Web Interface

Once the server is running, visit:

- **Home/Documentation**: http://localhost:8085
- **Full Menu**: http://localhost:8085/menu
- **Snacks Category**: http://localhost:8085/category/Snacks
- **Drinks Category**: http://localhost:8085/category/Drinks
- **Dinner Category**: http://localhost:8085/category/Diner
- **Lunch Category**: http://localhost:8085/category/Lunch

## Available Products

The service offers products across 4 categories:

### Snacks
- Chips - €2.50
- Cookie - €1.00

### Drinks
- Soda - €2.50
- Coffee - €1.50
- Tea - €1.50
- Juice - €3.00

### Diner
- Burger - €15.00
- Pizza - €12.50
- Salad - €9.50
- Sushi - €23.00

### Lunch
- Sandwich - €5.00
- Wrap - €5.50
- Soup - €3.50

## MCP Tools

### findBestMatchingProduct

Finds the best matching product for a provided product name using AI.

**Parameter:**
- `providedName` (String) - The product name to search for

**Returns:** Product object with name, description, price, and category

**Example Usage:**
- Input: "coke" → Output: Soda
- Input: "espresso" → Output: Coffee
- Input: "hamburger" → Output: Burger

The tool uses AI to intelligently match fuzzy or approximate product names to actual products in the catalog.

## AI Agent

### HandleOrderAgent

Processes food orders using natural language understanding:
- Natural language order processing
- Automatic product matching via `findBestMatchingProduct` tool
- Quantity handling
- Order confirmation

## Configuration

Server configuration is in `src/main/resources/application.yml`:

```yaml
server:
  port: 8085

embabel:
  models:
    default-llm: gpt-5-mini
    llms:
      best: gpt-5
      balanced: gpt-5-mini
```

## Architecture

The web interface follows the same pattern as the mcp-location server:

- **Controller**: `NomNomDocumentationController` handles web routes
- **Service**: `MenuService` provides menu data and product search
- **Templates**: Thymeleaf templates in `src/main/resources/templates/`
- **Static Assets**: Managed via WebJars (Bootstrap, jQuery)

## Integration with Main Application

The NomNom service integrates with the meeting planner web application through:

1. The `FoodAndDrinksAgent` in the agent module
2. The `NomNomAgent` in the web-app module
3. MCP protocol for tool invocation

## Development

```bash
# Build
mvn clean install -pl mcp-nomnom

# Run tests
mvn test -pl mcp-nomnom

# Run with hot reload (DevTools)
mvn spring-boot:run -pl mcp-nomnom
```

## Visual Design

The web interface uses:
- **Primary Color**: Green/Success theme (matching food service branding)
- **Accent Color**: Warning/Yellow (for prices and highlights)
- **Layout**: Bootstrap responsive grid
- **Icons**: Bootstrap Icons for enhanced UX
- **Cards**: Product cards with hover effects

Similar in style to the mcp-location server but with food service theming.
