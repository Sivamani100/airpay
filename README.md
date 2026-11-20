# Airpay — Sovereign Offline UPI Payment System

An open-source, zero-internet Android application that enables peer-to-peer Unified Payments Interface (UPI) transactions across India by communicating over standard cellular network signaling (*99# USSD).

<p align="center">
  <a href="https://github.com/Sivamani100/airpay"><img src="https://img.shields.io/badge/Version-1.0.0-5F259F.svg?style=for-the-badge" alt="Version 1.0.0" /></a>
  <a href="https://github.com/Sivamani100/airpay/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="MIT License" /></a>
  <img src="https://img.shields.io/badge/Network-100%25%20Offline%20USSD-00875A.svg?style=for-the-badge" alt="100% Offline" />
  <img src="https://img.shields.io/badge/Security-AES--256%20SQLCipher-00875A.svg?style=for-the-badge" alt="Encryption" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%2FMVVM-purple.svg?style=for-the-badge" alt="Architecture" />
</p>

---

## Table of Contents

1. [Executive Summary & Problem Statement](#1-executive-summary--problem-statement)
2. [Live Application Screenshots Gallery](#2-live-application-screenshots-gallery)
3. [Cellular Telecommunications Signaling Mechanics (*99# USSD)](#3-cellular-telecommunications-signaling-mechanics-99-ussd)
4. [Complete System Architecture & Module Hierarchy](#4-complete-system-architecture--module-hierarchy)
5. [Accessibility Node-Tree Automation Engine](#5-accessibility-node-tree-automation-engine)
6. [Visual Privacy Masking & Screen Shield](#6-visual-privacy-masking--screen-shield)
7. [Cryptography & Zero-Internet Security Model](#7-cryptography--zero-internet-security-model)
8. [Exhaustive Feature Guide & Operational Workflows](#8-exhaustive-feature-guide--operational-workflows)
9. [Multi-SIM Subscription Management & Carrier Routing](#9-multi-sim-subscription-management--carrier-routing)
10. [Computer Vision & Offline QR Pipeline](#10-computer-vision--offline-qr-pipeline)
11. [Direct Pre-Built Binary & Installation Guide](#11-direct-pre-built-binary--installation-guide)
12. [Developer Setup, Build Instructions & Testing](#12-developer-setup-build-instructions--testing)
13. [Permissions & Privacy Justification](#13-permissions--privacy-justification)
14. [Creator & Maintainer Profile](#14-creator--maintainer-profile)
15. [License & Open Source Notice](#15-license--open-source-notice)

---

## 1. Executive Summary & Problem Statement

The Unified Payments Interface (UPI) handles billions of financial transactions monthly. However, virtually every consumer payment app exhibits an absolute dependency on **TCP/IP packet data (4G, 5G, or Wi-Fi)**:

```
[ Traditional App ] ──▶ [ Packet Data (4G/5G/Wi-Fi) ] ──▶ [ Payment Gateway ] ──▶ [ Bank Core ]
                                       ▲
                                       │ FAILS WHEN:
                                       ├─ Basement parking lots & underground metro stations
                                       ├─ Deep rural & agrarian agricultural belts
                                       ├─ Congested stadium gatherings, rallies & festivals
                                       ├─ Optical fiber cuts & regional ISP outages
                                       └─ Depleted mobile data packs or carrier throttling
```

When packet connectivity fails, digital commerce freezes completely.

### The Sovereign Offline Solution

Airpay routes financial directives directly through the **cellular telecommunications baseband signaling plane** (SS7/MAP) using the national `*99#` USSD infrastructure established by NPCI. Because USSD operates over the dedicated signaling channel (SDCCH), it requires **zero bytes of mobile data** and functions reliably wherever basic 2G, 3G, 4G, or 5G voice reception is available.

---

## 2. Live Application Screenshots Gallery

<p align="center">
  <img src="docs/images/real_screen_home.jpg" width="24%" alt="Home Dashboard" />
  <img src="docs/images/real_screen_profile.jpg" width="24%" alt="Profile and Bank Details" />
  <img src="docs/images/real_screen_myqr.jpg" width="24%" alt="Offline QR Code" />
  <img src="docs/images/real_screen_balance.jpg" width="24%" alt="Available Balance Modal" />
</p>

---

## 3. Cellular Telecommunications Signaling Mechanics (*99# USSD)

Unstructured Supplementary Service Data (USSD) is a session-oriented GSM signaling capability that maintains an open bidirectional connection between the mobile station and the network gateway.

```
┌──────────────────┐         Cellular Signaling (SS7/MAP)         ┌─────────────────────┐
│  Mobile Device   │ ───────────────────────────────────────────▶ │ Telecom Base Station│
│  (Airpay Client) │ ◀─────────────────────────────────────────── │     (BTS / eNodeB)  │
└──────────────────┘                                              └──────────┬──────────┘
                                                                             │
                                                                             ▼
┌──────────────────┐               Direct Gateway API             ┌─────────────────────┐
│  Core Banking    │ ◀─────────────────────────────────────────── │ National *99# USSD  │
│  System (CBS)    │ ───────────────────────────────────────────▶ │   Gateway (NPCI)    │
└──────────────────┘                                              └─────────────────────┘
```

### Why USSD Works When Data Fails:
1. **Dedicated Signaling Bandwidth**: USSD communicates over the Standalone Dedicated Control Channel (SDCCH), entirely separate from GPRS/LTE packet data channels.
2. **Zero Packet Latency**: No DNS resolution, TLS handshake overhead, or TCP connection state is involved.
3. **Session-Oriented**: The carrier maintains an active radio bearer session throughout the transaction dialogue until final confirmation.

---

## 4. Complete System Architecture & Module Hierarchy

Airpay is engineered strictly as a **100% native Android application** in Kotlin, utilizing Clean Architecture principles with unidirectional data flow (MVI/MVVM).

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                              │
│       Jetpack Compose • Material 3 • Navigation • Custom Tokens        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ State / Intent
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                           Domain Layer                                 │
│        ActionRunner FSM • Session Sequencers • Input Validators        │
└───────────┬───────────────────────┬────────────────────────────┬───────┘
            │                       │                            │
            ▼                       ▼                            ▼
┌───────────────────────┐ ┌───────────────────┐ ┌────────────────────────┐
│  Telephony & SIM      │ │  Accessibility    │ │  Data & Persistence    │
│  Subscription Manager │ │  Automation &     │ │  Room Database         │
│  TelephonyManager     │ │  Privacy Overlay  │ │  SQLCipher (AES-256)   │
└───────────────────────┘ └───────────────────┘ └────────────────────────┘
```

### Package & Module Structure:
- **`presentation/screens/`**: Declarative Compose UI screens (HomeScreen, ContactChatScreen, ProfileScreen, ScanScreen, HistoryScreen, BalanceScreen, ToBankScreen).
- **`domain/`**: Financial business models (`Actions.kt`, `SessionState.kt`, `ActionRunner.kt`, `InputValidator.kt`).
- **`platform/`**: Telephony hooks (`SimInfo.kt`, `AirpayAccessibilityService.kt`, `OverlayController.kt`, `ContactsHelper.kt`).
- **`data/`**: Room entities, DAOs, SQLite database with SQLCipher encryption (`AirpayDatabase.kt`, `TransactionEntity.kt`).

---

## 5. Accessibility Node-Tree Automation Engine

Orchestrating standard *99# USSD dialogues manually requires dialing multi-level text menus, waiting for prompts, and typing options. Airpay automates this entire process through a dedicated background accessibility engine (`AirpayAccessibilityService`):

1. **Window Event Interception**: Listens to system-level `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED` events triggered by carrier dialog windows.
2. **Node-Tree Traversal**: Analyzes the active `AccessibilityNodeInfo` hierarchy to identify carrier response prompts (e.g., "Enter UPI PIN", "Select Account", "Enter Amount").
3. **Automated Submission**: Synthesizes text injection into the carrier's `EditText` field and triggers `ACTION_CLICK` on the confirmation button without human latency or dialing errors.
4. **Finite State Machine**: Manages transitions between session initiation, recipient validation, PIN prompt, confirmation, and timeout recovery.

---

## 6. Visual Privacy Masking & Screen Shield

To prevent sensitive information (such as dialing codes, transaction amounts, and PIN prompts) from being exposed or captured by screen-recording tools, Airpay employs a hardware-accelerated privacy overlay:

- **System Overlay Window**: Uses `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` to cover the entire screen the instant a USSD transaction begins.
- **Shoulder-Surfing Defense**: Replaces raw carrier dialogs with a modern, branded Airpay processing card that conceals carrier numbers.
- **Automated Dismissal**: The shield dismisses automatically as soon as the carrier confirms transaction completion.

---

## 7. Cryptography & Zero-Internet Security Model

### Absolute Zero-Internet Guarantee
Airpay intentionally omits `android.permission.INTERNET` from its `AndroidManifest.xml`. At the Android OS kernel level, the application is strictly prohibited from opening sockets, transmitting HTTP/HTTPS requests, or sending background analytics.

### Encrypted Persistence (SQLCipher AES-256)
- **Local Database**: All transaction records, beneficiaries, and cached profile details are stored in an encrypted Room SQLite database powered by SQLCipher.
- **Cipher Standard**: 256-bit AES encryption in CBC/GCM mode.
- **Key Generation**: Encryption keys are generated and protected by the hardware-backed Android Keystore System.

### Ephemeral Memory Buffers
UPI PINs are held solely in memory-pinned character buffers during transaction dispatch and are immediately zeroed out upon session termination. PINs are never stored in SharedPreferences, databases, or logs.

---

## 8. Exhaustive Feature Guide & Operational Workflows

### 8.1. Send Money to Mobile Number
- **USSD Format**: `*99*1*1*<Mobile>*<Amount>*<Remarks>#`
- **Workflow**:
  1. User selects a contact or enters any 10-digit phone number.
  2. Local contacts resolver fetches the contact name and opens a PhonePe-style payment chat view.
  3. User enters the amount and taps Send.
  4. Privacy overlay activates; accessibility daemon submits the user's UPI PIN into the carrier session.
  5. Transaction receipt is logged to encrypted storage with haptic confirmation.

### 8.2. Send Money to Bank Account
- **USSD Format**: `*99*1*5*<IFSC>*<Account>*<Amount>*<Remarks>#`
- **Workflow**:
  1. User inputs recipient Bank Account Number and 11-digit IFSC code.
  2. On-device regex checks validate the IFSC structure.
  3. Automated dispatch triggers direct bank-to-bank settlement over the national switch.

### 8.3. Send Money to UPI ID (VPA)
- **USSD Format**: `*99*1*3*<VPA>*<Amount>*<Remarks>#`
- **Workflow**:
  1. User enters any valid UPI ID (`username@bank`).
  2. The VPA format is validated locally.
  3. Background signaling routes funds directly to the target payment address.

### 8.4. Real-time Account Balance Inquiry
- **USSD Format**: `*99*3*1#`
- **Workflow**:
  1. User taps "Check Balance" on the home dashboard.
  2. User enters their secure UPI PIN.
  3. The carrier dialogue response is parsed, and the verified balance is displayed in a clean modal card.

### 8.5. Profile & Linked Bank Management
- **USSD Format**: `*99*4#`
- **Workflow**:
  1. Synchronizes registered user name and primary linked bank account directly from the SIM.
  2. **4-Day Smart Cache**: Automatically caches profile details locally to avoid unnecessary network queries.
  3. **Account Number Masking**: Protects account numbers by showing `•••• •••• 1234` by default with a 1-tap eye icon to unmask.

### 8.6. Additional Integrated USSD Services
- **Request Money (`*99*2#`)**: Issues offline collect requests to any mobile number or UPI ID.
- **Pending Requests (`*99*5#`)**: View and authorize incoming payment requests offline.
- **UPI PIN Management (`*99*7#`)**: Change or reset bank UPI PIN securely.
- **Change Linked Bank (`*99*4#`)**: Switch default linked bank account.
- **Past Statements & Rewards (`*99*6#`)**: Access carrier transaction records and promotional rewards.

---

## 9. Multi-SIM Subscription Management & Carrier Routing

On multi-SIM devices, routing signaling through the incorrect SIM card causes transactions to fail immediately because UPI is tied to the specific bank-registered phone number.

- **Dual-SIM Detection**: Airpay uses Android's `SubscriptionManager` to inspect active SIM slots (SIM 1 / SIM 2) and extract carrier identifiers (Jio, Airtel, Vi, BSNL).
- **SIM Slot Preference**: Users select their primary bank-registered SIM during setup or via Settings.
- **Targeted Intent Dispatch**: USSD calls are dispatched with explicit `android.telephony.extra.SUBSCRIPTION_ID`, ensuring the dialer always uses the correct carrier radio bearer.

---

## 10. Computer Vision & Offline QR Pipeline

- **CameraX Image Analysis**: High-performance camera frame analysis running locally on-device.
- **ZXing Core Decoder**: Decodes standard BharatQR and UPI QR formats (`upi://pay?pa=...&pn=...&am=...`) with zero internet access.
- **My QR Code Generator**: Generates clean, scannable offline QR codes for your phone number and VPA, complete with Download and Share capabilities.

---

## 11. Direct Pre-Built Binary & Installation Guide

### Download Release APK Directly
The pre-compiled production release APK is included in this repository:
- **Location**: `release/Airpay-v1.0.0.apk`
- **File Size**: 52.4 MB
- **Target OS**: Android 8.0 (API Level 26) through Android 15 (API Level 35)

### Installation Steps:
1. Download **`release/Airpay-v1.0.0.apk`** to your Android phone.
2. Open the file to install (enable "Install from unknown sources" if prompted).
3. Launch Airpay and complete the one-time permission setup:
   - **Phone & SIM**: Required to initiate *99# calls and select the active SIM.
   - **Accessibility**: Enables the automated USSD dialogue responder.
   - **System Overlay**: Enables the privacy shield during signaling.
4. Select your preferred bank-registered SIM card and begin transacting offline.

---

## 12. Developer Setup, Build Instructions & Testing

### Prerequisites
- Android Studio Ladybug (2024.2+) or later
- JDK 17 (Java Development Kit)
- Android SDK Platform 35 and Build-Tools 35.0.0
- Physical Android phone with an active SIM card (Reliance Jio, Bharti Airtel, Vi, BSNL)

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

The output APK is generated at `app/build/outputs/apk/release/app-release.apk`.

---

## 13. Permissions & Privacy Justification

| Permission | Android Manifest Identifier | Technical Necessity |
| :--- | :--- | :--- |
| **Call Phone** | `android.permission.CALL_PHONE` | Initiates *99# USSD telecommunication codes over cellular channels. |
| **Phone State** | `android.permission.READ_PHONE_STATE` | Detects SIM slot indices and carrier subscriptions via SubscriptionManager. |
| **Contacts** | `android.permission.READ_CONTACTS` | Resolves contact names for mobile numbers (purely local access). |
| **Camera** | `android.permission.CAMERA` | Powers the offline CameraX barcode analyzer for QR scanning. |
| **System Alert** | `android.permission.SYSTEM_ALERT_WINDOW` | Renders the privacy overlay to conceal USSD carrier dialogues. |
| **Accessibility** | `android.permission.BIND_ACCESSIBILITY_SERVICE` | Automates session dialog responses and PIN entry sequences. |

---

## 14. Creator & Maintainer Profile

<p align="center">
  <strong>Sivamanikanta Mallipurapu</strong><br>
  Full-Stack & Mobile Systems Developer
</p>

- 💼 **LinkedIn**: [Sivamanikanta Mallipurapu](https://www.linkedin.com/in/sivamanikanta-mallipurapu)
- 🐙 **GitHub**: [@Sivamani100](https://github.com/Sivamani100)
- 📧 **Email**: [mallipurapusiva@gmail.com](mailto:mallipurapusiva@gmail.com)

---

## 15. License & Open Source Notice

This project is licensed under the **[MIT License](LICENSE)**.
