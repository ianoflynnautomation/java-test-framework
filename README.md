# Java UI and Api Test Automation Framework

## Introduction

The Java Test Automation Framework built with Java that supports both API and UI testing using both BDD (Cucumber) and traditional JUnit approaches. This framework is designed to provide robust, scalable, and maintainable test automation for a BJJ (Brazilian Jiu-Jitsu) application.

**Key Technologies:**
- **Java 17** - Core programming language
- **Selenium WebDriver 4.22.0** - UI automation
- **Cucumber 7.18.0** - BDD testing framework
- **JUnit 5** - Traditional unit testing
- **Spring Boot 3.2.5** - Framework and WebClient for non-blocking API testing
- **Maven** - Dependency management and build tool
- **Docker** - Containerized test execution

**Testing Capabilities:**
- API testing with REST Assured and Spring WebClient
- UI testing with Selenium WebDriver
- BDD testing with Cucumber
- Traditional testing with JUnit 5
- Cross-browser testing support
- Parallel test execution
- Comprehensive reporting and observability

## Features

- **API Testing**
  - REST Assured for synchronous API testing
  - Spring WebClient for non-blocking, reactive API testing
  - Support for authentication, request/response validation
  - Built-in data factories and test data management

- **UI Testing**
  - Selenium WebDriver 4.22.0 with modern browser support
  - Page Object Model implementation
  - Cross-browser testing (Chrome, Firefox)
  - Screenshot capture and visual validation
  - Cookie and local storage management

- **BDD Testing**
  - Cucumber 7.18.0 with Gherkin syntax
  - Declarative style following BDD best practices
  - Feature files with comprehensive tagging system
  - Step definitions with dependency injection
  - Background scenarios and data tables

- **Traditional Testing**
  - JUnit 5 with modern annotations
  - Parameterized tests with CSV sources
  - Nested test classes for organization
  - Parallel execution support

- **Infrastructure & DevOps**
  - Docker containerization for consistent test environments
  - Selenium Grid for distributed testing
  - Maven for dependency management and build automation
  - Comprehensive logging and monitoring

- **Observability & Reporting**
  - TODO

## Prerequisites

- **Java Development Kit (JDK) 17** or higher
- **Apache Maven 3.6+**
- **Docker Desktop** with Docker Compose
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code)
- **Web Browsers** (Chrome, Firefox for UI testing)
- **Git** for version control

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd TestFramework
```

### 2. Build the Project
```bash
mvn clean install
```


## Project Structure

```
TestFramework/
├── bjjeire.core/                    # Core framework utilities and data models
├── bjjeire.api/                     # API testing framework components
├── bjjeire.selenium.web/            # Selenium WebDriver framework
├── tests/
│   ├── bjjeire.api.tests/           # JUnit-based API tests
│   ├── bjjeire.cucumber.api.tests/  # Cucumber-based API tests
│   ├── bjjeire.selenium.web.tests/  # JUnit-based UI tests
│   └── bjjeire.selenium.cucumber.web.tests/ # Cucumber-based UI tests
├── infra/                           # Infrastructure configuration
├── docker-compose.yml               # Docker services configuration
├── run-tests.sh                     # Test execution script
└── pom.xml                          # Maven parent POM
```

## How to Run Tests

### Command Line Execution

**Run All Tests:**
```bash
mvn clean test
```

**Run Specific Test Modules:**
```bash
# API Tests
mvn test -pl tests/bjjeire.api.tests
mvn test -pl tests/bjjeire.cucumber.api.tests

# UI Tests
mvn test -pl tests/bjjeire.selenium.web.tests
mvn test -pl tests/bjjeire.selenium.cucumber.web.tests
```

**Run Tests with Specific Tags:**
```bash
# Run only smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Run only API tests
mvn test -Dcucumber.filter.tags="@Type:API"

# Run only UI tests
mvn test -Dcucumber.filter.tags="@ui"
```

**Run Tests in Parallel:**
```bash
mvn test -Dparallel=true
```

### Docker Execution

**Run Tests in Docker:**
TODO

## Test Examples

### BDD - Cucumber Examples

#### API Test Feature File

**File:** `tests/bjjeire.cucumber.api.tests/src/test/resources/features/event_management.feature`
```gherkin
@Type:API @Feature:EventManagement
Feature: BJJ Event Management
  In order to provide users with accurate and up-to-date event listings,
  As an event organizer or user,
  I want to manage events.

  Background:
    Given Admin is authenticated

  Rule: Admin can create BJJ events

  @Type:Smoke @Type:Regression @Priority:High @TestCase:C789
  Scenario: Admin creates a new BJJ event with valid details
    Given a new event has been prepared
    When the Admin adds the new event
    Then the event should be successfully added

  Rule: Admin is prevented from creating events with invalid data
    
  @Type:Negative @Type:Regression @Priority:Medium @TestCase:C790
  Scenario Outline: Admin attempts to create a BJJ event with invalid data
    Given the Admin has an event with "<Field>" set to "<InvalidValue>"
    When the Admin attempts to add the new event
    Then the Admin should be notified that adding the event failed for "<Field>" with message "<ErrorMessage>"

    Examples:
      | Field               | InvalidValue | ErrorMessage                   |
      | Data.Name           |              | Event Name is required.        |
      | Data.Pricing.Amount |       -10.00 | Amount must be greater than 0. |
```

#### UI Test Feature File

**File:** `tests/bjjeire.selenium.cucumber.web.tests/src/test/resources/features/events/event_filtering.feature`
```gherkin
@ui @web @feature-events @Lifecycle:REUSE_IF_STARTED
Feature: Event Filtering
  In order to find relevant open mats, seminars and tournaments
  As a user of the BJJ app
  I want to filter events by county and type

  Background:
    Given I am a user of the BJJ app
    And I can access events

  Rule: Users can search events by county

    @smoke @regression @priority-high @Requirement-501 @TestCase-1002
    Scenario Outline: Filtering events by county
      Given the following BJJ events exist:
        | Name               | County   | Type       |
        | <County> Seminar 1 | <County> | Seminar    |
        | <County> Seminar 2 | <County> | Seminar    |
        | Dublin IBJJF       | Dublin   | Tournament |
      When I search events by county "<County>"
      Then I should see exactly <ExpectedCount> events for "<County>"

      Examples:
        | County  | ExpectedCount |
        | Cork    |             2 |
        | Kildare |             2 |

    @negative @regression @priority-medium @Requirement-504 @TestCase-1005
    Scenario: No events found for a county with no events
      Given the following BJJ events exist:
        | Name              | County | Type       |
        | Dublin Tournament | Dublin | Tournament |
      When I search events by county "Clare"
      Then I should not see any events

  Rule: Users can search events by type

    @regression @priority-high @Requirement-502 @TestCase-1003
    Scenario: Filtering events by type
      Given the following BJJ events exist:
        | Name           | County | Type       |
        | Cork Seminar 1 | Cork   | Seminar    |
        | Cork Seminar 2 | Cork   | Seminar    |
        | Cork IBJJF     | Cork   | Tournament |
      When I search for events of type "Seminar"
      Then I should see exactly 2 events of type "Seminar"

    @negative @regression @priority-medium @Requirement-505 @TestCase-1006
    Scenario: No events found when none exist
      Given no BJJ events exist
      When I search events by county "Wexford"
      Then I should not see any events
```

### JUnit Test Examples

The framework also supports traditional JUnit 5 testing for both API and UI testing. JUnit tests are located in:

- **API Tests:** `tests/bjjeire.api.tests/src/test/java/junit/`
- **UI Tests:** `tests/bjjeire.selenium.web.tests/src/test/java/junit/`

These tests provide the same functionality as Cucumber tests but use traditional JUnit annotations and assertions, making them ideal for developers who prefer a more programmatic approach to test automation.

## Reporting

### Test Execution Reports

After running tests, reports are generated in the following locations:

- **Cucumber Reports:** `target/cucumber-html-reports/`
- **JUnit Reports:** `target/surefire-reports/`
- **Test Results:** `test-results/` directory (created by run-tests.sh)

### Observability Stack

The framework includes a comprehensive observability stack accessible via Docker:

### Logging

- **Application Logs:** Configured via `logback.xml` in each test module
- **Test Execution Logs:** Stored in `logs/` directory
- **Docker Logs:** Accessible via `docker-compose logs [service-name]`

## Configuration

### Environment Configuration

The framework supports multiple environments through configuration files:

- `application.properties` - Default configuration
- `application-development.yml` - Development environment overrides
- `test-users.yml` - Test user credentials

### Browser Configuration

Supported browsers and configurations:

```java
@ExecutionBrowser(browser = Browser.FIREFOX, lifecycle = Lifecycle.REUSE_IF_STARTED)
@ExecutionBrowser(browser = Browser.CHROME, lifecycle = Lifecycle.PER_TEST)
```

### Parallel Execution

Configure parallel test execution in `junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
```

## Contributing

We welcome contributions to improve the BJJ Test Automation Framework! Here's how you can contribute:

1. **Fork the repository** on GitHub
2. **Create a feature branch** for your changes
3. **Make your changes** following the existing code style
4. **Add tests** for new functionality
5. **Update documentation** as needed
6. **Submit a pull request** with a clear description of your changes

### Development Guidelines

- Follow Java coding conventions
- Use meaningful commit messages
- Ensure all tests pass before submitting
- Update relevant documentation
- Follow the existing project structure

## Support

For questions, issues, or contributions:

- **Issues:** Create an issue in the GitHub repository
- **Documentation:** Check the project wiki and inline code comments
- **Community:** Join our development discussions

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Happy Testing! 🥋**