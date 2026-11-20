# Airpay — Sovereign Offline UPI Payment System

An open-source, sovereign, zero-internet Android application that enables real-time Unified Payments Interface (UPI) financial transactions across India by communicating over standard cellular baseband signaling (*99# USSD).

<p align="center">
  <a href="https://github.com/Sivamani100/airpay"><img src="https://img.shields.io/badge/Version-1.0.0-5F259F.svg?style=for-the-badge" alt="Version 1.0.0" /></a>
  <a href="https://github.com/Sivamani100/airpay/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="MIT License" /></a>
  <img src="https://img.shields.io/badge/Network-100%25%20Offline%20USSD-00875A.svg?style=for-the-badge" alt="100% Offline" />
  <img src="https://img.shields.io/badge/Security-AES--256%20SQLCipher-00875A.svg?style=for-the-badge" alt="Encryption" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%2FMVVM-purple.svg?style=for-the-badge" alt="Architecture" />
</p>

---

## Table of Contents
1. [Executive Briefing & Problem Statement](#1-executive-briefing--problem-statement)
2. [Live Application Showcase](#2-live-application-showcase)
3. [Telecommunications & Cellular Signaling Mechanics](#3-telecommunications--cellular-signaling-mechanics)
4. [End-to-End System Architecture](#4-end-to-end-system-architecture)
5. [Accessibility Automation & State Machine Pipeline](#5-accessibility-automation--state-machine-pipeline)
6. [Security, Cryptography & Threat Modeling](#6-security-cryptography--threat-modeling)
7. [Detailed Feature Guide & Operational Workflows](#7-detailed-feature-guide--operational-workflows)
8. [Computer Vision & Offline QR Pipeline](#8-computer-vision--offline-qr-pipeline)
9. [Direct Download & Installation](#9-direct-download--installation)
10. [Developer Setup, Building & Testing](#10-developer-setup-building--testing)
11. [Android Permissions Technical Reference](#11-android-permissions-technical-reference)
12. [Troubleshooting & Telecom FAQ](#12-troubleshooting--telecom-faq)
13. [Creator & Maintainer Profile](#13-creator--maintainer-profile)
14. [Open Source License](#14-open-source-license)

---

## 1. Executive Briefing & Problem Statement

The Unified Payments Interface (UPI) handles billions of financial transactions monthly. However, conventional UPI applications exhibit a fatal vulnerability: **an absolute dependency on Internet Protocol (TCP/IP) packet data (4G/5G/Wi-Fi)**.

```
[ Conventional UPI ] ──▶ [ Internet / Packet Data (4G/5G) ] ──▶ [ Cloud Servers ] ──▶ [ Bank Switch ]
                                        │
                                        ▼ FAILS DURING:
                                ├─ Basement parking & underground transit
                                ├─ Remote rural & agricultural corridors
                                ├─ Congested stadium crowds & festivals
                                ├─ Severe storms & optical fiber disruptions
                                └─ Mobile data depletion or carrier throttling
```

When packet data fails, payment apps freeze, network sockets timeout, and financial transactions grind to a halt.

### The Sovereign Offline Solution

Airpay bypasses the entire internet packet network by routing financial directives directly over the **cellular baseband signaling plane** (SS7/MAP) through national telecommunication gateways (*99# USSD). By automating multi-step telecommunication dialogues through native Android accessibility hooks, Airpay delivers a modern, tactile, and frictionless smartphone experience with **zero bytes of mobile data**.

---

## 2. Live Application Showcase

<p align="center">
  <img src="docs/images/real_screen_home.jpg" width="24%" alt="Home Dashboard" />
  <img src="docs/images/real_screen_profile.jpg" width="24%" alt="Profile and Bank Details" />
  <img src="docs/images/real_screen_myqr.jpg" width="24%" alt="Offline QR Code" />
  <img src="docs/images/real_screen_balance.jpg" width="24%" alt="Available Balance Modal" />
</p>

---

## 3. Telecommunications & Cellular Signaling Mechanics

### 3.1. 3GPP GSM Signaling Bearers
Unstructured Supplementary Service Data (USSD) is a session-oriented technology specified in 3GPP TS 22.090 and TS 24.090. Unlike SMS, which is store-and-forward, USSD establishes a real-time, bi-directional, session-based signaling connection between the mobile handset (MS) and the core network.

```
[ Handset ] ──(Radio Signaling)──▶ [ Base Station (BTS/NodeB) ]
                                             │
                                             ▼
                                [ Mobile Switching Center (MSC) ]
                                             │
                                             ▼
                                  [ Home Location Register (HLR) ]
                                             │
                                             ▼
                                   [ USSD Gateway (USSDGW) ]
                                             │
                                             ▼
                                [ National UPI Switch (*99#) ]
                                             │
                                             ▼
                                   [ Core Banking System (CBS) ]
```

- **Latency**: Sub-second signaling packet transmission over standalone dedicated control channels (SDCCH) or fast associated control channels (FACCH).
- **Zero Internet Requirement**: Handled entirely at the telecommunications signaling layer. No IP address or APN configuration is required.
- **Coverage**: Functions anywhere basic 2G voice reception is available.

### 3.2. National Unified USSD Platform (*99#) Protocol Matrix

Airpay formats and executes direct MMI action codes according to the National Unified USSD Platform specifications:

| Financial Operation | Direct Action Protocol | Execution Format |
| :--- | :--- | :--- |
| **Send to Mobile** | `*99*1*1#` | `*99*1*1*<Phone>*<Amount>*<Remarks>#` |
| **Send to UPI ID** | `*99*1*3#` | `*99*1*3*<VPA>*<Amount>*<Remarks>#` |
| **Send to Bank Account** | `*99*1*5#` | `*99*1*5*<IFSC>*<Account>*<Amount>*<Remarks>#` |
| **Check Balance** | `*99*3*1#` | Direct balance inquiry on primary account |
| **Request Money** | `*99*2#` | Collect request via phone or VPA |
| **My Details & SIM Sync** | `*99*4#` | Retrieves registered user name and linked bank |
| **Pending Requests** | `*99*5#` | Review and authorize inbound collect requests |
| **Past Transactions** | `*99*6*1#` | Pulls last verified transactions directly from carrier |
| **View Rewards** | `*99*6*2#` | Retrieves promotional vouchers and cashbacks |
| **UPI PIN Management** | `*99*7#` | Set, reset, or change account UPI PIN |

### 3.3. Multi-SIM Subscription Management
Airpay utilizes Android's `SubscriptionManager` API to support dual-SIM devices:
- Detects all active SIM card subscriptions in SIM Slot 1 and SIM Slot 2.
- Automatically queries carrier names (Reliance Jio, Bharti Airtel, Vodafone Idea, BSNL).
- Persists user SIM preferences and dispatches MMI intents bound explicitly to the chosen `SubscriptionInfo.subscriptionId`.

---

## 4. End-to-End System Architecture

Airpay is engineered strictly as a **100% native Android application** in Kotlin, utilizing Clean Architecture principles with unidirectional data flow (MVI/MVVM).

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presentation Layer                           │
│     Jetpack Compose • Material 3 • Navigation • Custom Tokens   │
└───────────────────────────────┬─────────────────────────────────┘
                                │ State / Intent
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Domain Layer                              │
│       ActionRunner FSM • Flow Sequencers • Input Validators     │
└───────┬───────────────────────┬─────────────────────────┬───────┘
        │                       │                         │
        ▼                       ▼                         ▼
┌───────────────┐       ┌───────────────┐         ┌───────────────┐
│ Telephony &   │       │ Accessibility │         │ Local Data &  │
│ Multi-SIM     │       │ Automation    │         │ SQLCipher     │
│ Subscription  │       │ & Overlay     │         │ Room (AES-256)│
└───────────────┘       └───────────────┘         └───────────────┘
```

---

## 5. Accessibility Automation & State Machine Pipeline

Because native Android does not expose an open programmatic API to input responses into carrier USSD popups on non-system apps, Airpay implements an on-device accessibility automation engine (`AirpayAccessibilityService`).

```
[ User Initiates Transfer ]
            │
            ▼
[ OverlayController Displays Privacy Mask ] (TYPE_APPLICATION_OVERLAY)
            │
            ▼
[ TelephonyManager Dispatches USSD Code ]
            │
            ▼
[ AccessibilityService Catches Window Event ] (TYPE_WINDOW_STATE_CHANGED)
            │
            ▼
[ Node Tree Traversal: Locates EditText & Buttons ]
            │
            ▼
[ ActionRunner Submits Step / PIN Automatically ]
            │
            ▼
[ Parser Extracts Confirmation / Error Message ]
            │
            ▼
[ Overlay Closes ──▶ UI Shows Verified Result Card ]
```

### Automation Guardrails & Reliability Mechanisms:
1. **Node Tree Inspection**: Recursively parses `AccessibilityNodeInfo` objects to locate text inputs (`android.widget.EditText`) and action buttons (`android.widget.Button`).
2. **Debounce & Race-Condition Guards**: Multi-step USSD sessions often emit multiple window content events. Airpay implements fingerprinting and window ID caching to prevent double-submitting steps.
3. **Slow-Carrier Watchdog Timers**: Accommodates carrier network latency with configurable watchdogs (12-second timeout window) to avoid false-positive timeout errors during network congestion.

---

## 6. Security, Cryptography & Threat Modeling

### 6.1. The Zero-Internet Principle
Airpay contains **zero networking libraries** (no Retrofit, OkHttp, Ktor, or Volley) and **omits `android.permission.INTERNET`** entirely from `AndroidManifest.xml`. At the Android OS and Linux kernel level, the application cannot establish network sockets, open HTTP connections, or transmit telemetry.

### 6.2. SQLCipher AES-256-GCM Database Encryption
All persistent data (transaction history, saved beneficiaries, cached profile details) is stored in a SQLite database encrypted with **SQLCipher**:
- **Algorithm**: 256-bit AES in GCM mode.
- **Key Storage**: The database passphrase is generated dynamically and stored securely inside the hardware-backed **Android Keystore System** (backed by TEE / StrongBox).

### 6.3. Ephemeral PIN Scrubbing
UPI PINs entered by the user are held exclusively in pinned in-memory `CharArray` buffers. The character array is explicitly scrubbed with null bytes (` `) immediately after submission to the accessibility daemon, eliminating memory-dump exposure risks.

### 6.4. Hardware-Accelerated Privacy Overlay
Using `android.permission.SYSTEM_ALERT_WINDOW`, Airpay projects a secure overlay (`TYPE_APPLICATION_OVERLAY`) over the display during transaction execution. This shields carrier dialogs, raw MMI strings, and sensitive credentials from visual inspection, shoulder surfing, and third-party screen-recording apps.

---

## 7. Detailed Feature Guide & Operational Workflows

### 7.1. Send Money to Mobile Number
- Resolves contact names locally from the address book.
- Opens an interactive PhonePe-style payment view with recent transaction history for that contact.
- Dispatches string `*99*1*1*<Phone>*<Amount>*<Remarks>#`.
- Collects UPI PIN in a clean dialog and submits it automatically through the signaling session.

### 7.2. Send Money to Bank Account
- Accepts recipient Bank Account Number and 11-character IFSC code.
- Enforces strict regex format validation on the IFSC (e.g., `SBIN0001234`, `HDFC0000128`).
- Dispatches string `*99*1*5*<IFSC>*<Account>*<Amount>*<Remarks>#`.

### 7.3. Send Money to UPI ID (VPA)
- Accepts any standard Virtual Payment Address (`username@bank`).
- Validates the VPA structure locally.
- Dispatches string `*99*1*3*<VPA>*<Amount>*<Remarks>#`.

### 7.4. Real-time Account Balance Inquiry
- Initiates `*99*3*1#` directly.
- Securely accepts the user's UPI PIN and extracts the available balance response into a formatted modal card with date and time stamp.

### 7.5. Profile & SIM Synchronization
- Initiates `*99*4#` to retrieve the registered account holder name and primary linked bank account.
- **4-Day Local Cache**: Caches details in the encrypted database to eliminate unnecessary signaling traffic.
- **Masked Account Digits**: Displays bank account digits as `•••• •••• 1234` with an interactive eye toggle to unmask full digits.

---

## 8. Computer Vision & Offline QR Pipeline

Airpay includes a native offline QR processing engine combining **AndroidX CameraX** and the **ZXing** barcode library:
- **Offline Scanning**: Processes image frames using CameraX `ImageAnalysis` on a dedicated background thread. Decodes BharatQR and UPI standard QR codes locally without internet.
- **My QR Code**: Generates personal offline UPI QR codes dynamically using ZXing bitmap matrices. Provides 1-tap options to download the QR code image or share it via system sheets.

---

## 9. Direct Download & Installation

### Option A: Install Pre-Built Release APK
The pre-compiled release APK is available directly in this repository:
- **Binary**: `release/Airpay-v1.0.0.apk`
- **Size**: 52.4 MB
- **Target OS**: Android 8.0 (API Level 26) through Android 15 (API Level 35)

### Installation Steps:
1. Download **`release/Airpay-v1.0.0.apk`** to your Android phone.
2. Open the file and tap **Install** (enable "Install from unknown sources" if prompted).
3. Launch Airpay and complete the one-time permission setup:
   - **Phone & SIM**: Grants permission to trigger USSD calls and detect dual-SIM subscriptions.
   - **Accessibility**: Enables the automated session responder.
   - **System Overlay**: Enables the privacy shield during transactions.
4. Select your preferred SIM card registered with your bank account and begin transacting offline.

---

## 10. Developer Setup, Building & Testing

### Prerequisites:
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 (Java Development Kit)
- Android SDK 26+ (Platform 35, Build-Tools 35.0.0)
- Physical Android phone with an active Indian SIM card (Reliance Jio, Airtel, Vi, BSNL)

### Build Commands:
```bash
# Clone the repository
git clone https://github.com/Sivamani100/airpay.git
cd airpay/LocalPe

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease
```

The output APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

---

## 11. Android Permissions Technical Reference

| Permission | Android Identifier | Purpose |
| :--- | :--- | :--- |
| **Call Phone** | `android.permission.CALL_PHONE` | Required to initiate *99# USSD telecommunication codes. |
| **Phone State** | `android.permission.READ_PHONE_STATE` | Required to detect SIM slot indices via SubscriptionManager. |
| **Contacts** | `android.permission.READ_CONTACTS` | Resolves names for saved mobile numbers (local access only). |
| **Camera** | `android.permission.CAMERA` | Powers offline QR code scanning via CameraX. |
| **System Alert** | `android.permission.SYSTEM_ALERT_WINDOW` | Displays the security overlay during USSD execution. |
| **Accessibility** | `android.permission.BIND_ACCESSIBILITY_SERVICE` | Automates multi-step carrier dialog responses. |

---

## 12. Troubleshooting & Telecom FAQ

### 1. "Unexpected Carrier Response"
- **Cause**: The cellular network took too long to respond or returned an unparsed message format.
- **Solution**: Check cellular signal strength and retry the transaction. In dual-SIM devices, verify that the active SIM is the one registered with your bank.

### 2. "Connection problem or invalid MMI code"
- **Cause**: Occurs on certain carriers when VoLTE or WiFi Calling interferes with USSD signaling channels.
- **Solution**: Ensure your phone is connected to cellular network towers. If using VoWiFi, temporarily switch off Wi-Fi calling.

### 3. "SIM Not Registered for UPI"
- **Cause**: The SIM card in your chosen slot is not registered with your bank's UPI service.
- **Solution**: Go to Profile -> Settings -> SIM Slot Preferences, and select the SIM slot containing your bank-registered mobile number.

---

## 13. Creator & Maintainer Profile

<p align="center">
  <strong>Sivamanikanta Mallipurapu</strong><br>
  Full-Stack & Mobile Systems Developer
</p>

- 💼 **LinkedIn**: [Sivamanikanta Mallipurapu](https://www.linkedin.com/in/sivamanikanta-mallipurapu)
- 🐙 **GitHub**: [@Sivamani100](https://github.com/Sivamani100)
- 📧 **Email**: [mallipurapusiva@gmail.com](mailto:mallipurapusiva@gmail.com)

---

## 14. Open Source License

This project is licensed under the **[MIT License](LICENSE)**.
