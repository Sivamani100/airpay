# Arkio Labs Contributing Guidelines

Thank you for your interest in contributing to **Downpay** (`com.arkiolabs.downpay`), developed and maintained by Arkio Labs Inc.

Downpay is a mission-critical offline financial utility engineered to enable fast, reliable, and secure UPI transactions without an active internet connection. To maintain the highest standards of financial security, code quality, and privacy compliance, all contributors must adhere strictly to these guidelines.

---

## 1. Core Architectural Pillars

### 1.1 Zero-Internet Policy (`android.permission.INTERNET` is strictly forbidden)
Downpay is fundamentally engineered around zero internet dependency:
- The Android Manifest **MUST NEVER** request `android.permission.INTERNET`.
- No HTTP/HTTPS networking libraries (e.g., OkHttp, Retrofit, Ktor, Volley) are permitted.
- No remote analytics, crashlytics, or telemetry SDKs are allowed.
- All credential handling, transaction storage, and data resolution must remain strictly on-device.

### 1.2 Telephony Engine Integrity
- Telephony dialing strings (`*99*...#`) and USSD intent dispatchers are strictly reserved for internal engine and service components.
- Raw USSD codes must never be surfaced as hardcoded labels or text within user-facing UI screens.
- All carrier state changes (Jio, Airtel, Vi, BSNL) must propagate through `ActionRunner` and `SessionState` state machines.

### 1.3 Cryptographic Protection at Rest
- All SQLite transaction data and cached user preferences must be encrypted using SQLCipher with AES-256 (`net.zetetic:android-database-sqlcipher`).
- User UPI PINs must never be persisted to permanent storage. PIN inputs are strictly in-memory, transient, masked, and immediately garbage-collected following USSD injection.

---

## 2. Code Quality & Standards

### 2.1 Kotlin Style
- 100% Kotlin codebase targeting JVM 17.
- Modern Jetpack Compose declarative UI paradigms. Avoid legacy XML views except for accessibility system overlays (`SYSTEM_ALERT_WINDOW`).
- Follow official Kotlin coding conventions: 4-space indentation, camelCase naming for variables/functions, PascalCase for classes and `@Composable` functions.

### 2.2 Testing Requirements
- Every new domain use case or platform parser must include corresponding Kotest property-based tests under `app/src/test/`.
- Ensure tests verify edge cases: dual-SIM slot switching, malformed SMS formats, incomplete USSD prompts, invalid VPA formats, and zero-balance responses.

### 2.3 Verification Workflow
Before submitting any Pull Request:
1. Run `./gradlew.bat test` to verify all unit and property tests pass.
2. Run `./gradlew.bat assembleDebug` to ensure clean compilation without warnings.
3. Verify `git status` reports zero untracked artifacts or cache files.

---

## 3. Intellectual Property & Contributor Agreement

By submitting code, patches, documentation, or visual assets to this repository, you explicitly agree that:
1. Your contributions become the proprietary intellectual property of Arkio Labs Inc.
2. You grant Arkio Labs Inc. an irrevocable, perpetual, worldwide, royalty-free, exclusive license to reproduce, adapt, license, and distribute your contribution.
3. You certify that your contribution is your own original work and does not infringe upon any third-party patent, copyright, trademark, trade secret, or proprietary right.

For security reports or vulnerability disclosures, please email: `mallipurapusiva@gmail.com`.
