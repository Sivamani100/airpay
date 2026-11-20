# Airpay

Airpay is an Android application for sending and receiving UPI payments without an internet connection. It communicates over mobile network signaling to process offline transactions.

## Highlights

- Offline peer-to-peer payments via phone number, bank account, and UPI ID
- On-device balance inquiry
- Offline QR code scanning and generation
- Local encrypted storage with SQLCipher
- Privacy protection during network operations

## Media

- Home Banner: `docs/images/hero_banner.jpg`
- Screenshots: `docs/images/screen_home.jpg`, `docs/images/screen_chat_pay.jpg`, `docs/images/screen_qr_scan.jpg`, `docs/images/screen_history.jpg`

## Requirements

- Android 8.0 (API level 26) or higher
- SIM card with active cellular service

## Build Instructions

```bash
git clone https://github.com/Sivamani100/airpay.git
cd airpay/LocalPe
./gradlew assembleDebug
```

## Author

- Siva Mallipurapu
- GitHub: [Sivamani100](https://github.com/Sivamani100)
- LinkedIn: [Siva Mallipurapu](https://linkedin.com/in/sivamallipurapu)
- Email: mallipurapusiva@gmail.com

## License

This software is released under the terms of the MIT License. See [LICENSE](LICENSE) for details.
