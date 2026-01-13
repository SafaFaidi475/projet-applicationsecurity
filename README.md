# 🛡️ SecureTeam Access  (v3.0 Expert Edition)

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
*   **QR Integration**: Native QR Code generation via `ZXing` (Base64) for seamless onboarding.

### 2. PASETO v2 Public (Beyond JWT)
SentinelKey explicitly rejects JWT (JSON Web Tokens) to eliminate common vulnerabilities (e.g., identity forged via `alg:none`).
*   **Platform-Agnostic Security Tokens (PASETO)**: Uses **v2.public** tokens (Asymmetric Ed25519 signatures).
*   **Tamper-Proof Payload**: Signatures are handled by `Bouncy Castle`, ensuring cryptographic integrity.
*   **Stateless with State-Full Revocation**: **JTI (JSON Token Identity)** tracking is implemented in Redis to prevent replay attacks and allow real-time token revocation.

### 3. Moteur ABAC (Attribute-Based Access Control)
Access decisions are computed in real-time based on a multi-dimensional matrix:
*   **Subject**: Authenticated identity with `department`, `roles`, and `authorized_projects`.
*   **Resource**: Scoped by `project_id` and action type.
*   **Environment**: Device ID, Source IP, and Business Hours constraints.

### 4. Steganography Service (Data Dissimulation)
SentinelKey includes a fully functional **Steganography Module** using **Least Significant Bit (LSB)** embedding.
*   **Cover Images**: Supports PNG and BMP for lossless data hiding.
*   **Bit-Level Precision**: Messages are embedded across RGB channels with length-prefix encoding.
*   **Usage**: Accessible via `/api/stego/hide` and `/api/stego/extract`.

---

## 📂 Project Structure

```bash
SentinelKey-Access-Broker/
├── frontend/                  # Lit-based PWA Dashboard
│   ├── src/                   
│   │   ├── services/          # api-client.js (Main integration layer)
│   │   └── secureteam-app.js  # Main UI & Router
│   └── vite.config.js         # Security Proxy
│
├── backend/                   # Jakarta EE Security Engine
│   ├── src/main/java/com/secureteam/
│   │   ├── auth/              # Security Filter, PASETO, TOTP, ABAC Engine
│   │   ├── api/               # User, Project, Stego, Audit, Key Resources
│   │   ├── model/             # Identity & Project Entities
│   │   └── steganography/     # LSB Implementation
│   └── pom.xml                # Hardened Dependency Management
│
├── infrastructure/            # Environment & CI/CD
└── docker-compose.prod.yml    # Production Orchestration
```

---

## 🛠️ Technical Stack & Justification

### Backend: Performance & Hardening
*   **Server**: WildFly 38.0.1.Final (Jakarta EE 11) - Robust security subsystems.
*   **Crypto**: Bouncy Castle (Ed25519) - Pure Java stability for Windows/Linux.
*   **Audit Logging**: Native tracking of all access decisions (Granted/Denied).
*   **Cache**: Redis 7.2 - TTL-based JTI tracking and MFA secret storage.

### Frontend: Integration Layer
*   **API Client**: Specialized `api-client.js` with automatic Bearer token injection and Device ID fingerprinting.
*   **View Layer**: Lit 3.x for atomic, framework-agnostic components.

---

## 🔐 API Reference (Extended)

| Endpoint | Method | Security | Description |
| :--- | :---: | :--- | :--- |
| `/auth/register` | `POST` | PermitAll | User registration with ABAC attributes. |
| `/auth/mfa/verify`| `POST` | PermitAll | TOTP Validation -> PASETO Issuer. |
| `/stego/hide` | `POST` | PASETO | Embed data in cover image. |
| `/projects` | `GET` | ABAC | Retrieve authorized projects. |
| `/audit` | `GET` | ADMIN | Access real-time security logs. |
| `/keys/public` | `GET` | PermitAll | PASETO Public Key distribution. |

---

## 🚀 Deployment Guide

### Option A: Local Development
```bash
# Backend
cd backend && mvn clean package wildfly:run

# Frontend
cd frontend && npm install && npm run dev
```

### Option B: Production (Docker)
```bash
docker-compose -f docker-compose.prod.yml up -d --build
```

---

## 🛡️ Vulnerabilities Resolved

| Issue | Resolution | Status |
| :--- | :--- | :---: |
| Native Sodium Crash | Migrated to **Bouncy Castle Ed25519**. | Fixed ✅ |
| Passive Authorization | Implemented **Real-time ABAC Evaluation**. | Fixed ✅ |
| Token Replay | Implemented **Redis JTI Revocation**. | Fixed ✅ |
| Stego Placeholder | Implemented **Functional LSB Module**. | Fixed ✅ |

---

## 📜 Licence & Versioning
*   **Version**: 3.1.0 (Integration Edition)
*   **License**: Proprietary - Developed for **SecureTeam Application Security**.
*   **Contact**: [Admin SecureTeam](mailto:admin@secureteam.me)

---
Vulnérabilités Identifiées
1. Secrets Codés en Dur (CWE-798)
Description: Des clés d'accès (accessKey, secretKey, masterPassword) sont présentes directement dans le code source Java ou dans des fichiers de propriétés par défaut. Fichiers affectés:

backend/src/main/java/com/secureteam/storage/MinioService.java
backend/src/main/resources/META-INF/microprofile-config.properties Risque: Critique. Si le code fuit, les attaquants ont un accès total aux services de stockage et de chiffrement.
2. Divulgation d'Informations via Logs (CWE-209)
Description: L'application utilise System.out.println et e.printStackTrace() qui exposent des informations internes et la trace de la pile d'exécution dans la sortie standard. Fichiers affectés:

backend/src/main/java/com/secureteam/auth/AuthResource.java
backend/src/main/java/com/secureteam/auth/TotpService.java
backend/src/main/java/com/secureteam/storage/MinioService.java
 Risque: Moyen. Facilite la reconnaissance pour un attaquant.
3. Implémentation TOTP Faible et Cryptographie "Maison" (CWE-327)
Description:

Le service TOTP (
TotpService.java
) réimplémente manuellement l'algorithme HMAC-SHA1 au lieu d'utiliser une bibliothèque éprouvée.
La fenêtre de validation autorise une dérive de +/- 5 minutes (fenêtre de -10 à +2 * 30s), ce qui est excessif et augmente le risque d'attaques par rejou (Replay Attack). Fichiers affectés: 
backend/src/main/java/com/secureteam/auth/TotpService.java
 Risque: Élevé.
4. Dépendances Obsolètes (CWE-1104)
Description: La dépendance aerogear-otp-java est en version 1.0.0 (très ancienne). Fichiers affectés: 
backend/pom.xml
 Risque: Moyen.

Modifications Proposées
Backend
1. Sécurisation des Credentials
[MODIFY] 
backend/src/main/java/com/secureteam/storage/MinioService.java
: Retirer les valeurs en dur et utiliser @ConfigProperty pour injecter les secrets.
[MODIFY] backend/src/main/resources/META-INF/microprofile-config.properties: Définir des valeurs par défaut vides ou sécurisées et documenter l'utilisation des variables d'environnement.
2. Assainissement des Logs
[MODIFY] 
backend/src/main/java/com/secureteam/auth/AuthResource.java
: Remplacer e.printStackTrace() par un Logger (JBoss Logging).
[MODIFY] 
backend/src/main/java/com/secureteam/auth/TotpService.java
: Supprimer System.out.println et e.printStackTrace(). Utiliser un Logger.
[MODIFY] 
backend/src/main/java/com/secureteam/storage/MinioService.java
: Supprimer les System.out.println.
3. Renforcement TOTP
[MODIFY] 
backend/src/main/java/com/secureteam/auth/TotpService.java
:
Réduire la fenêtre de dérive à +/- 1 minute (fenêtres de -2 à +2).
Nettoyer l'implémentation manuelle si possible ou la corriger pour être stricte.
4. Mise à jour Dépendances
[MODIFY] 
backend/pom.xml
: Mettre à jour aerogear-otp-java vers une version plus récente ou utiliser une alternative si disponible (e.g. java-otp). Après vérification, aerogear-otp est déprécié. On gardera l'implémentation manuelle mais on la sécurisera.
Plan de Vérification
Tests Automatisés
Compilation du projet : mvn clean compile
Lancement des tests unitaires existants : mvn test
Vérification Manuelle
Logs : Vérifier que les mots de passe n'apparaissent pas dans les logs au démarrage. Use mvn wildfly:run.
Setup MFA : Tenter d'enregistrer un utilisateur et vérifier que le QR code est généré sans erreur.
Verify MFA : Tester la validation TOTP avec un délai correct et incorrect (vérifier que +/- 5 minutes échoue si on réduit la fenêtre).
