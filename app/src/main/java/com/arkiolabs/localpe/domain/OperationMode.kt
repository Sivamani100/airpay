package com.arkiolabs.localpe.domain

/**
 * The three operation modes controlling how a USSD session is presented.
 *
 * - [AUTO] (default): Full takeover — branded overlay covers the carrier
 *   dialog so the user only ever sees LocalPe's UI during the session.
 * - [ADVANCED]: Auto-fills the carrier dialog while keeping it visible,
 *   with a small floating progress chip pinned under the status bar.
 *   The user watches the carrier dialog work in real time.
 * - [MANUAL]: System dialer fallback — copies recipient details to clipboard and
 *   opens the native Android dialer with the carrier code prefilled. The user
 *   completes the transaction manually. No accessibility permissions required.
 *
 * Order matters here: the UI shows them top-down, and AUTO must be first
 * because it's the default.
 */
enum class OperationMode { AUTO, ADVANCED, MANUAL }
