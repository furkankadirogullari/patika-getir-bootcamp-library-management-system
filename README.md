# 📚 Library Management System

This is a full-stack backend project for managing a library system built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **JWT Authentication**, **Spring Security**, **Docker**, **Swagger**, and **Spring WebFlux (Reactive)**.

## 🔧 Features

### 📌 User Management

* Register & Login with JWT authentication
* Role-based access control: `PATRON`, `LIBRARIAN`
* View current user details
* Librarian can:

  * View user details
  * Update user
  * Delete user

### 📚 Book Management

* Add, update, delete books (`LIBRARIAN`)
* Search books by:

  * ISBN
  * Title
  * Genre
  * Author
* Paginated search with optional keyword filter
* Real-time availability updates (WebFlux)

### 📖 Borrowing & Returning

* Patrons can:

  * Borrow available books
  * Return borrowed books
  * View personal borrow history
* System tracks:

  * Borrow & due dates
  * Book quantity
* Librarians can:

  * View all borrow history
  * View overdue books
  * Export overdue reports (CSV)

### 🧪 Testing

* Unit & Integration tests with **Spring Boot Test**
* Uses in-memory **H2** database for tests

### 🌐 API Documentation

* Swagger/OpenAPI integrated
* Available at `/swagger-ui/index.html`

---

## 🚀 Technologies Used

* Java 21
* Spring Boot 3.4.5
* Spring Security + JWT
* Spring Data JPA + Hibernate
* PostgreSQL
* Docker + Docker Compose
* Springdoc OpenAPI (Swagger UI)
* Spring WebFlux (Reactive)
* JUnit & WebTestClient (Testing)

---

## 🐳 Docker Setup

### Prerequisites:

* Docker & Docker Compose installed

### Steps:

```bash
# Build and start the app + PostgreSQL
docker-compose up --build
```

App runs on: [http://localhost:8080](http://localhost:8080)

---

## 🌐 API Endpoints (Summary)

### Auth

* `POST /api/users/register` → Register a user
* `POST /api/users/login` → Login and get JWT token

### Users

* `GET /api/users/me` → Get current user info
* `GET /api/users/{id}` → Get user by ID (LIBRARIAN)
* `PUT /api/users/{id}` → Update user (LIBRARIAN)
* `DELETE /api/users/{id}` → Delete user (LIBRARIAN)

### Books

* `POST /api/books` → Add book (LIBRARIAN)
* `PUT /api/books/{isbn}` → Update book (LIBRARIAN)
* `DELETE /api/books/{isbn}` → Delete book (LIBRARIAN)
* `GET /api/books` → Get all books (search/pagination)
* `GET /api/books/isbn/{isbn}` → Get book by ISBN
* `GET /api/books/title/{title}` → Get books by title
* `GET /api/books/genre/{genre}` → Get books by genre
* `GET /api/books/author/{author}` → Get books by author
* `GET /api/reactive/stream` → Real-time book availability updates (WebFlux)

### Borrowing

* `POST /api/borrows/borrow` → Borrow a book (PATRON)
* `POST /api/borrows/return/{borrowId}` → Return a borrowed book (PATRON)
* `GET /api/borrows/history` → Get user's borrow history (PATRON)
* `GET /api/borrows/history/all` → All borrow history (LIBRARIAN)
* `GET /api/borrows/overdue` → Overdue borrows (LIBRARIAN)
* `GET /api/borrows/overdue/report` → Export overdue report as CSV (LIBRARIAN)

---

## ✅ Running Tests

```bash
# Run tests
./mvnw test
```

* Integration tests use H2 in-memory database
* Coverage available via IntelliJ or external tools (e.g. Jacoco)

---

## 🔐 Authentication

All protected endpoints require a valid JWT token.

Use `/api/users/login` to get token. Then include in `Authorization` header:

```
Authorization: Bearer <your_token_here>
```

---

## 📂 Postman Collection

A full Postman collection is available under:

📁 `library-management-postman-collection.json`

Postman Collection Link : https://.postman.co/workspace/My-Workspace~47d7e245-0e13-4a98-8664-83cb15d49a8b/collection/43120842-5cb95f4b-2bb6-42b4-94fd-ef3f83de0606?action=share&creator=43120842

Import this into Postman to test endpoints with ease.

---

## ✍️ Author

Furkan Kadiroğulları

📧 [furkankadirogullari@gmail.com](mailto:furkankadirogullari@gmail.com)

---


