# Phoenix IAM

Identity and Access Management system built with Jakarta EE.

## Features
- OAuth 2.0 Authorization Code Flow with PKCE
- User authentication with Argon2 password hashing
- Consent management
- Tenant/Client management

## Tech Stack
- Jakarta EE
- JAX-RS
- CDI

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── xyz/kaaniche/phoenix/iam/
│   │       ├── boundaries/     # REST endpoints
│   │       ├── controllers/    # Business logic
│   │       ├── entities/       # Domain models
│   │       └── security/       # Security utilities
│   └── resources/
└── test/
```

## Getting Started

### Prerequisites
- Java 17+
- Maven
- Jakarta EE compatible server

### Build
```bash
mvn clean install
```