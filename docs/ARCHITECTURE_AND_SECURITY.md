# PHARB — Architecture, Security, Admin & Google Play Release Guide

## 1. Architectural Decisions & Scalability (1K → 100K → 1M+ Users)
- **Mobile Client (Android)**: Built with **Kotlin + Jetpack Compose + Material 3**, following **MVVM + Offline-First Repository Pattern** with **Room SQLite** local cache and **Retrofit** REST interfaces.
- **Database Layer**: **PostgreSQL 16** (`/database/schema.sql`) with composite B-Tree and trigram (`pg_trgm`) indexes for high-speed Arabic & English search, paired with **Redis 7** for session caching, sliding-window rate limiting, and real-time pub/sub queues.
- **Media & Short Videos Pipeline**: **PHARB Shorts** and **Stories** use Object Storage (S3-compatible) + **CDN** with HLS/H.265 Adaptive Bitrate streaming so low-end devices and variable networks experience smooth playback without storing heavy blobs in the relational database.

## 2. Security & Privacy Architecture
- **Zero Plaintext Passwords**: Passwords are salted with 128-bit `SecureRandom` salts and hashed via **PBKDF2-HMAC-SHA256** (`SecurityManager.kt`).
- **Session & Token Security**: Signed **JWT Access Tokens** + **Refresh Tokens** with per-device session tracking and 1-tap remote session revocation.
- **Input Sanitization & Rate Limiting**: Built-in protection against XSS, SQL injection, and brute-force login/post spam.
- **Privacy Center & GDPR Compliance**: Users control account visibility (`Public` / `Private`), messaging permissions, online/last-seen indicators, data portability (`JSON` export), and permanent **Delete Account**.

## 3. Google Play Release Checklist (Section 50)
- **Unique Application ID**: `com.aistudio.pharbsocial.vqxknr`
- **Custom Adaptive Icon**: Configured in `mipmap-anydpi-v26` and density-specific PNGs (`mdpi` through `xxxhdpi`) with Deep Navy (`#0B192C`) and PHARB geometric prism monogram.
- **Permissions Compliance**: Zero broad storage permissions; uses least-privilege `INTERNET`, `ACCESS_NETWORK_STATE`, and `VIBRATE`.
