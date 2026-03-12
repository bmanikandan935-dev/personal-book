# Personal Book Service

## Project Overview
A Spring Boot backend that integrates with the Google Books API to search volumes and persist selected books into an H2 in-memory database. The API exposes endpoints to list saved books, search Google Books, and create a saved book from a Google volume ID.

## Architecture
Package structure follows clean architecture and SOLID principles:

- `com.example.books.controller` REST controllers only
- `com.example.books.service` business logic and external API calls
- `com.example.books.repository` JPA repositories
- `com.example.books.entity` JPA entities
- `com.example.books.dto` transport DTOs
- `com.example.books.mapper` MapStruct mappers
- `com.example.books.config` configuration beans
- `com.example.books.exception` custom exceptions and global handler

Key rules enforced:
- Controllers contain no business logic
- External Google API calls are isolated in `GoogleBookService`
- Mapping uses MapStruct
- Validation happens before persistence
- Centralized exception handling
- SLF4J logging at API, service, validation, and error paths

## Package Structure
```text
com.example.books
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
└── service
```

## Configuration
Set your Google Books API key with an environment variable or directly in `src/main/resources/application.properties`:

```
google.books.base-url=https://www.googleapis.com/books/v1
google.books.api.key=${GOOGLE_BOOKS_API_KEY:}
```

Optional client timeout settings:

```
google.books.connect-timeout-ms=5000
google.books.read-timeout-ms=10000
```

For local development on Windows:

```
setx GOOGLE_BOOKS_API_KEY "YOUR_API_KEY"
```

## Build
```
./mvnw clean test
```

Windows:

```powershell
.\mvnw.cmd clean test
```

## Run
```
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## API Endpoints
- `GET /books` - list saved books
- `GET /google?q=searchTerm` - Google Books search (raw schema)
- `POST /books/{googleId}` - fetch volume from Google and persist

## Example cURL
Search:
```
curl "http://localhost:8080/google?q=effective+java"
```

Create and save a book:
```
curl -X POST "http://localhost:8080/books/lRtdEAAAQBAJ"
```

List saved books:
```
curl "http://localhost:8080/books"
```

## Testing Notes
- `BookIntegrationTest` calls the real Google API and is skipped unless `GOOGLE_BOOKS_API_KEY` is set.
- `GoogleApiKeyTest` verifies that outbound Google API calls include the configured `key` query parameter.
- MockWebServer tests cover Google API error handling (404/500/504).
- `ExistingEndpointsTest` verifies `GET /books` and `GET /google`.

