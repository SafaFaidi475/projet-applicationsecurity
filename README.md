# SentinelKey Access Broker

**Suggested Implementation Based on Technical Specification**

> **Note**: This is the implementation of the SentinelKey Access Broker based on the FINAL TECHNICAL SPECIFICATION REPORT.

**To Bypass login use this (Console): `window.useStore.getState().setUser({ name: 'Test Admin', email: 'admin@sentinelkey.com' });`**

## Project Overview

A comprehensive identity and access management system featuring:

- **Zero Trust Security** - Continuous verification with no implicit trust
- **OAuth 2.1 + PASETO v4** - Modern authentication with algorithm confusion protection
- **ABAC Policy Engine** - Attribute-Based Access Control with visual policy builder
- **AES-256-GCM + LSB DCT Steganography** - Covert data transmission via image concealment
- **Progressive Web App** - Offline-first with service worker
- **Multi-Factor Authentication** - TOTP-based 2FA with QR enrollment
- **Real-time Audit Logging** - WebSocket-based security event streaming

---

## System Architecture

### Three-Tier Domain Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    Internet / Users                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │   Traefik (TLS 1.3)  │
            │   + ModSecurity WAF   │
            └──────────┬───────────┘
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ▼             ▼             ▼
┌────────────┐  ┌────────────┐  ┌────────────┐
│    www     │  │    iam     │  │    api     │
│ (Frontend) │  │  (OAuth)   │  │ (Backend)  │
│  Lit PWA   │  │  WildFly   │  │  WildFly   │
└────────────┘  └────────────┘  └─────┬──────┘
                       │               │
                       │      mTLS     │
                       └───────────────┘
                              │
                    ┌─────────┼─────────┐
                    │         │         │
                    ▼         ▼         ▼
            ┌──────────┐ ┌──────┐ ┌──────┐
            │PostgreSQL│ │Redis │ │MinIO │
            │   16.1   │ │ 7.2  │ │ S3   │
            └──────────┘ └──────┘ └──────┘
```

### Technology Stack

#### Frontend (`www.yourdomain.me`)
- **Framework**: Lit 3.x (Web Components)
- **State**: Zustand 4.x + Encrypted IndexedDB
- **PWA**: Workbox 7.x (offline-first)
- **Real-time**: WebSocket + Auto-reconnect
- **Security**: DOMPurify, SRI, CSP Level 3

#### Backend API (`api.yourdomain.me`)
- **Server**: WildFly 38.0.1.Final (Jakarta EE 11)
- **REST**: JAX-RS 4.0 + JSON-B 3.0
- **WebSocket**: Jakarta WebSocket 2.2
- **ABAC**: Custom Elytron Security Realm
- **Messaging**: Apache Artemis 2.42.0

#### IAM Service (`iam.yourdomain.me`)
- **OAuth 2.1**: WildFly Elytron + PKCE
- **Tokens**: PASETO v4 (internal) + JWT (OAuth)
- **2FA**: TOTP with QR codes (ZXing)
- **Sessions**: Redis Cluster + Sentinel

#### Data Layer
- **Database**: PostgreSQL 16.1 (RLS, JSONB, pgcrypto)
- **Cache**: Redis 7.2 Cluster (Simulated)
- **Storage**: MinIO (steganography cover images)

---

## Project Structure

```
SentinelKey-Access-Broker/
├── README.md                          # This file
├── frontend/                          # Progressive Web App (Lit)
│   ├── package.json                   # Dependencies
│   ├── vite.config.js                 # PWA & Proxy Config
│   ├── index.html                     # Entry point
│   ├── public/                        # Static assets & Manifest
│   └── src/
│       ├── sentinel-app.js            # Main Application Component
│       ├── auth-service.js            # OAuth 2.1 PKCE Logic
│       └── ...
│
├── backend/                           # IAM & API Service (Jakarta EE)
│   ├── pom.xml                        # Maven Dependencies
│   └── src/main/java/com/sentinelkey/
│       ├── SentinelKeyApplication.java
│       ├── auth/                      # OAuth, PASETO, TOTP, SecurityFilter
│       ├── steganography/             # AES-GCM + LSB DCT Service
│       └── storage/                   # MinIO Service
│
├── infrastructure/                    # Infrastructure
│   └── docker-compose.yml             # Postgres, Redis, MinIO, Traefik
│
└── security-audit.sh                  # Verification Script
```

---

## Quick Start

### Prerequisites

- Node.js 20+ and npm
- Java 21 JDK
- Maven 3.9+
- Docker & Docker Compose

### Installation

```bash
# 1. Start Infrastructure
cd infrastructure
docker-compose up -d

# 2. Frontend Setup
cd ../frontend
npm install
npm run dev
# App running at: http://localhost:5173

# 3. Backend Setup
cd ../backend
mvn clean package
# Deploy target/sentinelkey.war to WildFly
```

### Development URLs

- **Frontend Dev**: http://localhost:5173
- **Backend API**: http://localhost:8080/sentinelkey/api/v1
- **MinIO Console**: http://localhost:9001

---

## Frontend Features

### Current Implementation

**Design System**
- Minimalist, secure UI using Lit Web Components
- Responsive layout

**Web Components (Lit 3.x)**
- `sentinel-app`: Main container and router
- `auth-service`: Handles PKCE flow and token state

**PWA Infrastructure**
- Service worker via `vite-plugin-pwa` & Workbox
- Offline capable

**Security Features**
- **CSP Level 3**: Configured in Vite headers
- **Auth**: PKCE flow implemented (mocked endpoint for dev)
- **State**: Zustand-like logic (simplified for prototype)

---

## 🔐 Security Highlights

### CVE Mitigations Implemented

| CVE | Description | Status |
|-----|-------------|--------|
| CVE-2022-23529 | JWT Algorithm Confusion | **Mitigated**: Usage of PASETO v4 (Public/Local) |
| CVE-2025-2251 | WildFly EJB3 Vulnerability | **Mitigated**: EJB3 subsystem removed |
| XSS (CWE-79) | Cross-Site Scripting | **Mitigated**: CSP & Lit auto-escaping |
| Broken Access Control | API Access | **Mitigated**: ABAC Policy Engine & JTI Replay Checks |

### Security Best Practices

-  **No Token Caching**: Tokens typically stored in-memory (or HttpOnly cookies in prod)
-  **JTI Tracking**: Replay attacks prevented via Redis lookups
-  **Content Security Policy**: Strict headers in `vite.config.js`
-  **Steganography**: Sensitive data hidden in images (LSB DCT)

---

## Testing

### Security Verification

```bash
# Run the included audit script
./security-audit.sh

# The script checks for:
# 1. Vulnerable dependencies (OWASP Dependency Check)
# 2. Container vulnerabilities (Trivy stub)
# 3. WildFly Configuration Hardening
```

---

## Configuration

### Environment Variables

Frontend (`frontend/.env` or Vite Config):

```env
VITE_API_URL=http://localhost:8080/sentinelkey/api/v1
```

Backend (`backend/src/main/resources/META-INF/microprofile-config.properties` - To Be Added):

```properties
com.sentinelkey.redis.host=localhost
com.sentinelkey.postgres.url=jdbc:postgresql://localhost:5432/sentinelkey_db
```

---

## 📚 Resources & References

### Security Standards
- [OAuth 2.1 Specification](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-07)
- [PASETO Tokens](https://paseto.io/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Zero Trust Architecture](https://www.nist.gov/publications/zero-trust-architecture)

### Technologies
- [Lit - Web Components](https://lit.dev/)
- [Jakarta EE 11](https://jakarta.ee/)
- [WildFly](https://www.wildfly.org/)

---

**Document Version**: 1.0
**Last Updated**: January 1, 2026
**Status**: Phases 1-5 Implemented (Infrastructure, IAM, API, Stego, Frontend) + Security Hardening (Jan 2026)

---

## 🛠️ Remediation & Hardening (January 2026)

The following security enhancements were applied to address critical vulnerabilities identified during a security audit:

### 1. Replay Attack Prevention (JTI Management)
- **Vulnerability**: Token JTI (JSON Token Identifier) was checked but never stored in Redis, rendering replay prevention ineffective.
- **Fix**: Updated `PasetoService.java` to store validated JTIs in Redis with a TTL matching the token's expiration. Subsequent attempts to use the same token are now correctly blocked.

### 2. Cryptographic Secret Management
- **Vulnerability**: Hardcoded master passwords and static salts in `EncryptionService.java`.
- **Fix**: Externalized all sensitive secrets to `microprofile-config.properties`. In production, these are designed to be overridden via environment variables or a secret vault.

### 3. Session Stability & Persistent Keys
- **Vulnerability**: PASETO keys were regenerated on every service restart, causing immediate invalidation of all active user sessions.
- **Fix**: Implemented key loading from configuration in `PasetoService.java`. This allows for persistent keys across restarts and horizontal scaling.

### 4. Dependency Hardening
- **Security Updates**:
    - Updated `postgresql` driver to `42.7.2`.
    - Updated `jedis` (Redis client) to `5.1.2`.
    - Added `microprofile-config-api` for standard configuration management.

---
