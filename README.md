# 🛡️ SentinelKey Access Broker (v3.0 Expert Edition)

**Next-Generation Zero Trust Identity & Access Management (IAM) for Critical Infrastructures.**

[![Security: Zero Trust](https://img.shields.io/badge/Security-Zero--Trust-blueviolet?style=for-the-badge)](https://github.com/)
[![Protocol: PASETO v2 Public](https://img.shields.io/badge/Protocol-PASETO_v2_Public-blue?style=for-the-badge)](https://paseto.io/)
[![Auth: MFA TOTP](https://img.shields.io/badge/Auth-MFA_TOTP_RFC6238-success?style=for-the-badge)](https://rootprojects.org/totp/)
[![Engine: Jakarta EE 11](https://img.shields.io/badge/Engine-Jakarta_EE_11-orange?style=for-the-badge)](https://jakarta.ee/)
[![Status: Production Ready](https://img.shields.io/badge/Status-Production_Ready-green?style=for-the-badge)](https://github.com/)

---

## 📖 Project Overview

**SentinelKey** is a specialized Access Broker designed for high-security environments. It implements a **Zero Trust Architecture (ZTA)** focusing on **Just-In-Time (JIT)** access and **Attribute-Based Access Control (ABAC)**. Unlike traditional IAM solutions, SentinelKey assumes no implicit trust and requires continuous verification for every access attempt, enriched by device fingerprinting and temporal constraints.

---

## 🧠 Core Security Architecture

### 1. Advanced MFA: Adaptive TOTP (RFC 6238)
The authentication flow utilizes Time-Based One-Time Passwords with enhanced resilience:
*   **Clock-Drift Management**: Implements a manual verification window of **+/- 3 minutes (360s)** to ensure usability across client devices with slight time desynchronization.
*   **Secure Seeding**: MFA secrets are generated using `SecureRandom`, Base32 encoded, and stored in **Redis** with a strict 10-minute TTL during the setup phase.
*   **QR Integration**: Native QR Code generation via `ZXing` (Base64) for seamless onboarding with Google Authenticator, Authy, or Microsoft Authenticator.

### 2. PASETO v2 Public (Beyond JWT)
SentinelKey explicitly rejects JWT (JSON Web Tokens) to eliminate common vulnerabilities (e.g., identity forged via `alg:none` or key-confusion attacks).
*   **Platform-Agnostic Security Tokens (PASETO)**: Uses **v2.public** tokens (Asymmetric Ed25519 signatures).
*   **Tamper-Proof Payload**: Signatures are handled by `Bouncy Castle`, ensuring cryptographic integrity without reliance on potentially unstable native libraries (libsodium).
*   **Stateless with State-Full Revocation**: While tokens are stateless, **JTI (JSON Token Identity)** tracking is implemented in Redis to prevent replay attacks and allow real-time token revocation.

### 3. Moteur ABAC (Attribute-Based Access Control)
Access decisions are computed in real-time based on a multi-dimensional matrix:
*   **Subject**: Authenticated identity (via PASETO).
*   **Resource**: Granular resource ID (Resource/Project level).
*   **Action**: Capability-based (CRUD).
*   **Environment**: Device Fingerprint (DeviceID), Source IP, and Business Hours (Temporal constraints).

### 4. Steganography Service (Dissimulation)
For ultra-secure data exchange, SentinelKey includes a **Steganography Module** designed for **Least Significant Bit (LSB)** and **Discrete Cosine Transform (DCT)** embedding.
> [!NOTE]
> The current bridge implementation is in STUB mode to maintain environment compatibility, ready for full OpenCV integration in high-security air-gapped zones.

---

## 🛠️ Technical Stack & Justification

### Backend: Performance & Hardening
*   **Server**: WildFly 38.0.1.Final (Jakarta EE 11) - Selected for its robust security subsystems and CDI 4.1 support.
*   **Crypto**: Bouncy Castle (Ed25519) - Chosen over native providers for cross-platform stability (Windows/Linux).
*   **Cache**: Redis 7.2 - Handles ephemeral secrets (TOTP) and JTI revocation lists with nanosecond latency.
*   **Database**: PostgreSQL 16.1 - Relational integrity for identity management and audit logs.

### Frontend: Modern PWA Experience
*   **View Layer**: Lit 3.x - Lightweight Web Components for maximum performance and minimum bundle size.
*   **Lifecycle**: Vite - For rapid development and optimized production builds.
*   **Resilience**: Workbox PWA - Service Worker integration for offline capabilities and secure asset caching.
*   **State**: Zustand 4.x - Minimalist and high-performance state management.

---

## 📂 Project Structure

```bash
SentinelKey-Access-Broker/
├── frontend/                  # Lit-based PWA Dashboard
│   ├── src/                   
│   │   ├── auth-service.js    # PASETO handling & session logic
│   │   └── secureteam-app.js  # Main UI Router & Components
│   └── vite.config.js         # Security Proxy & Build config
│
├── backend/                   # Jakarta EE Security Engine
│   ├── src/main/java/         
│   │   ├── auth/              # PASETO, TOTP, Redis Logic
│   │   ├── model/             # Identity & Security Entities
│   │   └── steganography/     # Dissimulation Module
│   └── pom.xml                # Hardened Dependency Management
│
├── infrastructure/            # Environment & CI/CD
└── docker-compose.prod.yml    # Production Orchestration
```

---

## 🚀 Deployment Guide

### Option A: Local Development (Manual)
1.  **Redis**: Ensure Redis is running on `localhost:6379`.
2.  **Backend**:
    ```bash
    cd backend
    mvn clean package wildfly:run
    ```
3.  **Frontend**:
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

### Option B: Production (Docker Orchestration)
The project includes a production-ready `docker-compose.prod.yml` with:
*   **Traefik**: Reverse proxy with automatic SSL (Let's Encrypt).
*   **Hardened Network**: Internal bridge network `secureteam-net`.
*   **Healthchecks**: PostgreSQL readiness probes.

```bash
docker-compose -f docker-compose.prod.yml up -d --build
```

---

## 🔐 API Reference (Core)

| Endpoint | Method | Security | Description |
| :--- | :---: | :--- | :--- |
| `/auth/mfa/setup` | `GET` | PermitAll | Generates TOTP secret & QR Code Image. |
| `/auth/mfa/verify` | `POST` | PermitAll | Validates TOTP code & issues PASETO token. |
| `/auth/health` | `GET` | PermitAll | Security Engine status check. |

---

## 🛡️ Vulnerabilities Resolved (Audit Log)

| CVE / Issue | Resolution | Status |
| :--- | :--- | :---: |
| Native Sodium Crash | Migrated to pure Java **Bouncy Castle Ed25519**. | Fixed ✅ |
| Token Replay | Implemented **Redis JTI Tracking**. | Fixed ✅ |
| MFA Desync | Added **+/- 3min Clock Drift tolerance**. | Optimized ✅ |
| CORS Leakage | Implemented **Vite Secure Proxy** in dev. | Hardened ✅ |

---

## 📜 Licence & Versioning
*   **Version**: 3.0.0 (Expert Edition)
*   **License**: Proprietary - Developed for **SecureTeam Application Security**.
*   **Contact**: [Admin SecureTeam](mailto:admin@secureteam.me)

---
*Generated with 🛡️ by SecureTeam Security Suite.*
