# SDET User API Challenge

A Java-based API testing framework built around TestNG, OkHttp, Allure, and YAML-driven test data.

The project is designed to exercise a user management API running locally in Docker or in CI pipeline, with environment-specific base URLs configured by `-Denv=dev|prod`.

## Repository Structure

```
.
├── .github/
├── .gitignore
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/ben/sdet/
│   │   │   ├── client/
│   │   │   │   ├── BaseClient.java
│   │   │   │   └── UserApi.java
│   │   │   ├── common/Result.java
│   │   │   ├── config/
│   │   │   │   ├── ConfigProvider.java
│   │   │   │   ├── ServiceConfig.java
│   │   │   │   └── UserServiceConfig.java
│   │   │   ├── factory/HttpClientFactory.java
│   │   │   ├── logging/
│   │   │   │   ├── JsonUtil.java
│   │   │   │   ├── LoggerUtils.java
│   │   │   │   └── LoggingInterceptor.java
│   │   │   ├── service/UserService.java
│   │   │   └── utils/
│   │   │       ├── ObjectMapperProvider.java
│   │   │       └── RetryUtil.java
│   │   └── resources/config/
│   │       ├── dev.properties
│   │       └── prod.properties
│   └── test/
│       ├── java/com/ben/sdet/
│       │   ├── data/
│        │   │   ├──TestUserFactory.java
│       │   │   └── UserDataProvider.java
│       │   ├── dto/UserTestData.java
│       │   ├── tests/
│       │   │   ├── UserReadTests.java
│       │   │   ├── UserTests.java
│       │   │   └── UserWriteTests.java
│       │   ├── utils/
│       │   │   ├── data/TestDataUtils.java
│       │   │   └── YamlLoader.java            
│       │   └── validators/UserValidator.java
│       └── resources/
│           ├── allure.properties
│           ├── logback.xml
│           ├── testdata/users.yaml
│           └── testng.xml
```

## What this project does

- Implements a reusable API test framework for a user service
- Uses OkHttp for HTTP requests and Jackson for JSON/YAML handling
- Loads environment configuration from `src/main/resources/config/{dev,prod}.properties`
- Drives tests from YAML data in `src/test/resources/testdata/users.yaml`
- Wraps API responses in a generic `Result<T>` object with success/error handling
- Supports a TestNG suite plus single-method debug runs via VS Code

## Run locally

### 1. Start the local API service in Docker

```bash
docker run --rm -p 3000:3000 ghcr.io/danielsilva-loanpro/sdet-interview-challenge:latest
```

This exposes the API at `http://localhost:3000`.

### 2. Run the tests

Run the default TestNG suite:

```bash
mvn test
```

Run a single test method:

```bash
mvn -Dtest=com.ben.sdet.tests.UserTests#shouldNotLeakUsersAcrossEnvironments test
```

Run using the suite XML explicitly:

```bash
mvn -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml test
```

### 3. Run in a specific environment

The framework loads `env` from a system property, defaulting to `dev`.

```bash
mvn -Denv=prod test
```

## Configuration files

- `src/main/resources/config/dev.properties` — points to `http://localhost:3000/dev`
- `src/main/resources/config/prod.properties` — points to `http://localhost:3000/prod`

## Design decisions

- `ConfigProvider` loads environment-specific properties from classpath resources
- `BaseClient` centralizes OkHttp request execution and response parsing
- `UserApi` contains API endpoints; `UserService` provides a service layer around it
- `Result<T>` preserves status code, parsed data, raw body, and error payload
- Logging uses an Allure-aware interceptor to attach request/response payloads
- YAML-driven test data separates test cases from code

## Notes

- The project uses Maven and Java 21
- You can inspect the generated allure results in `target/allure-results`
- The test framework is intended for integration-style API validation, not unit testing

## GitHub Actions

This repository includes GitHub Actions workflows that run the API test suite for both `dev` and `prod` environments.

Use the GitHub Actions tab in the repository UI, select the `User API Tests` workflow, and click `Run workflow` to trigger it on demand.

- Workflow files:
  - `.github/workflows/tests.yml`
  - `.github/workflows/reusable-tests.yml`
- Triggers:
  - `push`
  - `pull_request`
  - `workflow_dispatch`
- Jobs:
  - `test-dev` — runs with `-Denv=dev`
  - `test-prod` — runs with `-Denv=prod`

Each workflow:
- checks out the repository
- sets up Java 21
- caches Maven dependencies
- starts the API Docker container on `http://localhost:3000`
- waits for the API to be ready
- runs:
  ```bash
  mvn clean test -Denv=<dev|prod> -DsuiteXmlFile=src/test/resources/testng.xml
  ```
- generates an Allure report
- uploads the report as a workflow artifact

To run the same flow locally, use the same Maven command with the matching environment:

```bash
mvn clean test -Denv=dev -DsuiteXmlFile=src/test/resources/testng.xml
```

## Project Status & Known Gaps

This repository includes an in-progress test plan and bug report in `TEST_PLAN_AND_BUG_REPORT.md`.

- The current test plan is not fully covered by the existing automated tests.
- There are pending fixes needed in the tests and API validation assertions.
- Formatter and cleanup tasks are still required to improve code quality.
- Additional test coverage is needed to better validate edge cases and error handling.
- Java documentation is currently missing and should be added.
- The token used for verifying PUT /users/{email} endpoints should be store in a secure place.
