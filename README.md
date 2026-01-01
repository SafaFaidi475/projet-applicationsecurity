# SentinelKey Access Broker

**Secure Enterprise PWA with Zero Trust Architecture & Data Dissimulation**

SentinelKey is a high-security Access Broker designed to protect sensitive enterprise data. It employs Zero Trust principles, strict access controls (ABAC), and advanced data concealment techniques (Steganography).

## 🚀 Key Features

*   **Zero Trust Architecture**: Never trust, always verify.
*   **PASETO v4 Tokens**: Secure, non-manipulatable tokens (v2 Public/Local compliant).
*   **OAuth 2.1 Authorization Server**: Implementing PKCE and strict state validation.
*   **TOTP 2FA**: Time-based One-Time Passwords standard (RFC 6238).
*   **Data Dissimulation**: AES-256-GCM Encryption hidden within images via LSB DCT Steganography.
*   **Offline PWA**: Full functionality offline via Workbox.

## 🛠️ Technology Stack

*   **Backend**: WildFly 38.0.1.Final Preview (Jakarta EE 11)
*   **Database**: PostgreSQL 16.1 (with RLS & pgcrypto)
*   **Identity**: Redis 7.2 Cluster (Session/Rate Limiting)
*   **Encryption**: Bouncy Castle (AES-256-GCM), OpenCV (Steganography)
*   **Frontend**: Lit 3.x, Zustand, Workbox

## 🏗️ Project Structure

```
SentinelKey-Access-Broker/
├── backend/                  # Jakarta EE 11 Backend
│   ├── src/main/java/        # Source Code (OAuth, PASETO, Stego)
│   └── pom.xml               # Maven Dependencies
├── frontend/                 # Lit 3.x PWA
│   ├── src/                  # Components & Services
│   ├── package.json          # Node Dependencies
│   └── vite.config.js        # Build Config
├── infrastructure/           # Docker Setup
│   └── docker-compose.yml    # Postgres, Redis, MinIO
└── security-audit.sh         # Verification Script
```

## ⚙️ Setup & Installation

### Prerequisites
*   Java 21
*   Maven 3.9+
*   Docker & Docker Compose
*   Node.js 20+ (for Frontend)

### 1. Infrastructure
Start the required services:
```bash
cd infrastructure
docker-compose up -d
```

### 2. Backend
Build and deploy the WAR file:
```bash
cd backend
mvn clean package
# Deploy target/sentinelkey.war to your WildFly server
```

### 3. Frontend
Install dependencies and run dev server:
```bash
cd frontend
npm install
npm run dev
```

## 🔒 Security Mitigations

| Vulnerability | Mitigation Strategy |
| :--- | :--- |
| **Token Replay** | Redis-backed JTI tracking (One-time use validation) |
| **CSRF** | OAuth State parameter with strong entropy & validation |
| **XSS** | CSP Level 3, DOMPurify, Lit's auto-escaping |
| **CVE-2025-2251** | Removal of EJB3 subsystem in WildFly config |

## 🧪 Verification

Run the included security audit script:
```bash
./security-audit.sh
```

---
**Developed for Advanced AppSec Project**
