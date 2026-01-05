# SecureTeam Access – Temporary & Context-Aware Access Management System

**The definitive solution for managing temporary, collaborative, and high-security technical access.**

## 🛡️ Project Overview

**SecureTeam Access** is a specialized Identity and Access Management (IAM) platform designed to solve a critical business problem: **how to securely manage temporary access for external collaborators (freelancers, consultants, partners) and sensitive administrative tasks.**

Unlike generic IAM solutions, SecureTeam Access focuses on **temporal and contextual constraints**, ensuring that access is granted only when needed (Just-In-Time) and revoked automatically when it's not.

---

## 🧠 Core Functional Concepts

### 1. Temporary Access Management (TTL-Based Identities)
*   **Automatic Expiry**: Users are created with a predefined "End of Mission" date.
*   **Life-Cycle Automation**: Once the expiration date is reached, the account is automatically locked, and all active sessions are invalidated.

### 2. Context-Aware IAM (Continuous Verification)
Decisions are not made based on roles alone. Access is granted only if the current **context** matches the security policy:
*   **Project Context**: Access is restricted to specific projects or repositories.
*   **Temporal Context**: Access is only granted during specific time windows (e.g., 9:00 AM - 6:00 PM).
*   **Device Fingerprinting**: Access is tied to the specific workstation/device used by the collaborator.

### 3. Just-In-Time (JIT) & Approval Workflow
*   **On-Demand Access**: Users can request elevated permissions for a limited time.
*   **Mandatory Approval**: Sensitive requests must be validated by a manager before any rights are granted.
*   **Time-Limited Elevation**: Once the task is finished (or time runs out), the user reverts to their original base permissions.

---

## 🔐 Security Architecture (Zero Trust)

SecureTeam Access is built on a "Never Trust, Always Verify" foundation:
*   **OAuth 2.1 + PKCE**: Modern authentication flow protecting against code injection.
*   **PASETO v4 (Public)**: Using Platform-Agnostic Security Tokens for tamper-proof, algorithm-hardened identity.
*   **ABAC Engine**: Attribute-Based Access Control logic that evaluates user, project, time, and environment attributes simultaneously.
*   **AES-256-GCM + Steganography**: Sensitive audit logs and keys can be covertly transmitted via image dissimulation to bypass deep-packet inspection.

---

## 🏗️ Technical Stack

### Backend (SecureTeam IAM)
*   **WildFly 38.0.1.Final** (Jakarta EE 11)
*   **Redis 7.2** (Real-time JTI tracking and session state)
*   **PostgreSQL 16.1** (Persistent identity storage with RLS)

### Frontend (Management Dashboard)
*   **Lit 3.x** (Web Components)
*   **PWA** (Offline-first architecture for remote collaborators)

---

## 📂 Project Structure

```
SecureTeam-Access/
├── frontend/                          # Management Dashboard (Lit PWA)
│   ├── src/
│   │   ├── request-access.js          # JIT Request UI
│   │   ├── admin-approvals.js         # Manager Approval Workflow
│   │   └── ...
│
├── backend/                           # IAM & API Service
│   └── src/main/java/com/secureteam/
│       ├── auth/                      # OAuth, PASETO, ABAC logic
│       ├── approvals/                 # Workflow service
│       └── ...
│
└── infrastructure/                    # Docker & Traefik Config
```

---

## 🚀 Vision

SecureTeam Access replaces the "Static IAM" model with a **Fluid Security** model. It enables organizations to scale their collaboration with external teams without compromising on security, providing total visibility and control over who has access to what, when, and from where.

---

**Document Version**: 2.0 (Overhaul)
**Status**: SecureTeam Core Implemented.
