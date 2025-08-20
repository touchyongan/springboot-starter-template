# Project Structure Guideline

---

## Overview

This project follows a feature-based modular structure for better scalability, separation of concerns, 
and ease of maintenance. Instead of grouping classes by their technical role globally (e.g., all 
controllers in one package), we organize by domain feature, with each feature encapsulating its own 
layers (controller, service, data, etc.).

This approach improves:

- **Feature encapsulation** — changes are localized to one module
- **Team collaboration** — multiple teams can work on features independently
- **Scalability** — structure grows naturally with business complexity
- **Clean architecture** — supports domain-driven design principles

---

## Project Package Structure

```textmate
com.example.projectname
│
├── config/               ← Global application configuration
├── infrastructure/       ← Technical integration & infrastructure
├── common/               ← Shared utilities, exceptions, base classes
├── feature/              ← Domain-driven features (user, auth, product, etc.)
│   └── user/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── data/
│       ├── mapper/
│       └── validator/
│
└── Application.java       ← Spring Boot main application class
```

---

## Concept Origin

This structure is inspired by modern backend architecture practices including:

- Domain-Driven Design (DDD) — group code by business domain
- Vertical slicing — each feature is self-contained
- Hexagonal / Onion Architecture — core domain is independent of frameworks

Rather than placing all services or repositories in one global folder, each feature contains its own layers. 
This improves maintainability and makes it easier to navigate the codebase.

---

## Package Descriptions

### `config/`

Global application-wide configuration classes:

- `SecurityConfig.java`: Spring Security setup
- `WebConfig.java`: CORS, message converters, formatters
- Any other global Spring `@Configuration` beans

> 📌 These configurations are framework-level and affect the entire application.

---

### `infrastructure/`

Code that deals with external systems or technical concerns:

- Persistence setup (JPA, JDBC, Mongo)
- Messaging (Kafka, RabbitMQ)
- Email or third-party APIs

> 📌 Keeps your core domain logic decoupled from technical implementations.

---

### `common/`

Houses shared code across features:

- `exception/`: Custom exceptions and global exception handling
- `util/`: Utility/helper classes
- `constants/`: Application-wide constants
- `base/`: Base controller/service classes

> 📌 This is the "toolbox" for the whole project — reusable and not feature-specific.

---

### `feature/`

Core part of the application — each feature represents a business capability (e.g., user, auth, order, product).

Each feature contains its own layers:

#### ▸ `controller/`

- REST API endpoints for the feature
- Maps HTTP requests to service layer
- Contains request/response models if needed

#### ▸ `service/`

- Business logic of the feature
- Uses repository, mappers, and validators

#### ▸ `repository/`

- JPA or data-access interfaces
- Custom queries or persistence methods

#### ▸ `entity/`

- JPA entity classes (usually maps to database)
- Used by repository and sometimes service

#### ▸ `data/`

- DTOs (Data Transfer Objects)
- Used for incoming/outgoing data (e.g., request, response, command models)

#### ▸ `mapper/`

- Maps between DTOs ↔ entities
- May use tools like MapStruct, or manual mapping

#### ▸ `validator/`

- Feature-specific input validators
- Encapsulates validation rules outside controllers/services

> 📌 Each feature acts like a "mini-application" within the monolith or microservice.

---

## Best Practices

- Keep cross-feature dependencies minimal — prefer composition via services or mappers.
- If a component is only used in one feature, keep it inside that feature.
- Shared logic? Move it to common/ if it’s reused across multiple features.
- Avoid large utils/ dumping ground — name utilities by context (DateUtil, JwtUtil, etc.).
- Name classes clearly: UserService, UserValidator, UserCreateRequest, etc.

---

## Example: Feature - user

```textmate
feature/
└── user/
    ├── controller/
    │   └── UserController.java
    ├── service/
    │   └── UserService.java
    ├── repository/
    │   └── UserRepository.java
    ├── entity/
    │   └── User.java
    ├── data/
    │   ├── UserCreateRequest.java
    │   ├── UserData.java
    ├── mapper/
    │   └── UserMapper.java
    └── validator/
        └── UserValidator.java
```

---

## Benefits of This Structure

- 🧩 Encapsulation — Features are self-contained
- 🧼 Clean architecture — Layers are separated clearly
- 🧠 Readability — Easy for new devs to navigate
- 🧪 Testability — Easier to mock and unit test
- 🔧 Maintainability — Add/change a feature without breaking others
