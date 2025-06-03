# Logistic API

Sample API for logistics management, built with Kotlin + Spring Boot + Exposed + SQLite.

## Requirements
- Docker and Docker CLI (recommended: [Colima](https://github.com/abiosoft/colima) for macOS)
- (Optional) Make

## Running the Application

### Using Makefile (recommended)

- **Build and run with SQLite database persistence:**
  ```sh
  make run
  ```
  This command builds the Docker image and starts the API at `http://localhost:8080`, persisting the `logistic.db` file outside the container.

- **Build only:**
  ```sh
  make build
  ```

- **Clean Docker image:**
  ```sh
  make clean
  ```

### Manually with Docker

- Build:
  ```sh
  docker build -t logistic-api .
  ```
- Run with persistence:
  ```sh
  docker run -p 8080:8080 -v $(pwd)/logistic.db:/app/logistic.db logistic-api
  ```

## Notes
- The SQLite database is created automatically on application startup.
- No external services are required.
- The API will be available at `http://localhost:8080`.

## API Endpoints

### LogisticController

- **POST /api/email**
  - **Headers:**
    - `X-Client-Id`: Client UUID (required)
  - **Body:** JSON with logistic data (`LogisticRequest`)
  - **Description:** Submits logistic data and returns the created booking ID.

### LogisticOperationController

- **GET /api/orders**
  - **Headers:**
    - `X-Client-Id`: Client UUID (required)
  - **Description:** Returns all orders associated with the client.

- **GET /api/containers**
  - **Headers:**
    - `X-Client-Id`: Client UUID (required)
  - **Description:** Returns all containers associated with the client.

- **GET /api/orders/{purchaseId}/containers**
  - **Path variable:**
    - `purchaseId`: Purchase order UUID
  - **Description:** Returns containers associated with a specific purchase order.

- **GET /api/containers/{containerId}/orders**
  - **Path variable:**
    - `containerId`: Container UUID
  - **Description:** Returns orders associated with a specific container.

## Technical Considerations

### Best Practices Implemented

- Clean architecture: clear separation between controllers, services, repositories, and models.
- Input data validation using Jakarta Validation annotations (@Valid, @Validated).
- Centralized error handling with a global ExceptionHandler.
- Use of DTOs for requests and responses, decoupling API from internal domain.
- Dependency injection via constructor injection.
- Use of ResponseEntity for typed and controlled HTTP responses.
- Use of UUID as a secure and unique identifier for resources.
- Decoupled persistence using repositories and Exposed ORM.
- Unit and integration tests for controllers and repositories.
- Externalized configuration in application.properties.
- Structured logging with kotlin-logging.

### Nice to Have / Technical Debt

- Automatic API documentation (Swagger/OpenAPI).
- Advanced security/authentication (e.g., JWT or API Key).
- Pagination and filtering in query endpoints.
- API versioning support.
- Improved test coverage (error cases, validations, etc.).
- Health checks and metrics for monitoring.

---

For any questions, check the source code or contact the author.
