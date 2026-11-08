# Thombre Transport — Fleet & Trip Management System (VRMS)

[![Build Android APK](https://github.com/OWNER/REPOSITORY/actions/workflows/build-apk.yml/badge.svg)](https://github.com/OWNER/REPOSITORY/actions/workflows/build-apk.yml)
![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--35)-brightgreen)
![Language](https://img.shields.io/badge/Language-Kotlin%202.0%2B-blue)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-red)
![Database](https://img.shields.io/badge/Database-Jetpack%20Room%20(SQLite)-orange)
![License](https://img.shields.io/badge/License-Proprietary%20(Thombre%20Transport)-darkred)

A stand-alone, **100% offline-first Android mobile application** engineered for logistics and road transport operations. The system serves as an operational trip ledger, billing compilation engine, and PDF invoice generation tool for **Thombre Transport** (Baur Gaon, Taluka - Maval, Dist - Pune).

---

## 🌟 Key Architecture Highlights

* **100% Offline Operation:** No internet connection, cloud servers, or external databases are required. All records and PDFs are generated and stored locally on the Android handset.
* **Continuous Global Sequence:** Bill numbers auto-increment indefinitely starting from **`10001`** across the entire fleet.
* **Dynamic Archetype Forms:** Forms automatically adapt between **AC Vehicles** (Fixed Matrix: Source, Destination, Invoice No, Toll, Amount) and **Non-AC / Other Vehicles** (Distance Matrix: KM, Rate, Strike-Through Discount, Net Amount).
* **Indian Number-to-Words Engine:** Dynamic mathematical conversion of figures into the Indian Numbering System (*Rupees ... and XX Paise Only*) rendered in real-time.
* **On-Device Vector PDF Generator:** Pixel-perfect A4 invoice generation using native Android 2D Canvas without third-party cloud conversion tools.
* **Native OS Sharing:** Immediate launch of the Android Native Share Sheet via `FileProvider` upon invoice generation for instant delivery via WhatsApp, Business WhatsApp, or local printing.
* **Unified .vrms Disaster Recovery:** Bundles the complete SQLite database and all saved PDF files into a single compressed `.vrms` (`.zip`) archive via Android's Storage Access Framework (SAF).

---

## 📱 Supported Android Devices & Compatibility

* **Minimum SDK:** Android 8.0 (Oreo, API level 26)
* **Target SDK:** Android 14 / Android 15 (API level 34–35)
* **Hardware Support:** Optimized to run smoothly on any Android phone (Samsung, Xiaomi/Redmi, Realme, Vivo, Oppo, OnePlus, Motorola, etc.) including budget devices with 2 GB – 3 GB RAM.

---

## 🚀 Automated GitHub Actions CI/CD (No Local Setup Required)

This repository includes a pre-configured GitHub Actions workflow (`.github/workflows/build-apk.yml`) that automatically compiles and produces a ready-to-test APK on every push.

### How to Build & Download the APK:

1. **Push your code to GitHub:**
   ```bash
   git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPO_NAME>.git
   git branch -M main
   git push -u origin main
   ```

2. **Download the compiled APK:**
   * Open your GitHub repository in your web browser.
   * Navigate to the **Actions** tab at the top.
   * Click on the latest workflow run: **"Build & Package APK"**.
   * Under the **Artifacts** section at the bottom, click on **`Thombre-Transport-VRMS-Debug-APK`**.
   * Extract the downloaded `.zip` file to get `app-debug.apk`.
   * Transfer or open `app-debug.apk` directly on Mr. Thombre's phone and tap **Install**.

---

## 📂 Project Structure

```text
com.thombretransport.vrms/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs (Vehicle, Trip, Bill, AppConfig, History)
│   │   ├── database/     # AppDatabase.kt (Pre-seeded with #10001 & Profile)
│   │   └── entity/       # Room SQLite Entities
├── domain/
│   └── util/
│       ├── IndianNumberToWordsConverter.kt   # Lakhs, Crores, Paise Converter
│       └── RegistrationNumberValidator.kt    # RTO & Bharat Series Regex Filter
├── engine/
│   ├── backup/           # ZipBackupManager.kt (.vrms DB + PDF Archiver)
│   └── pdf/              # PdfInvoiceGenerator.kt (A4 Canvas 595x842 pt Renderer)
└── presentation/
    ├── billhistory/      # BillHistoryScreen.kt (PDF view/share, Out-of-sync badge)
    ├── billselector/     # BillCompilationScreen.kt (Trip multi-selector & generator)
    ├── components/       # Common Compose widgets (App bar, Popups, Date picker)
    ├── dashboard/        # FleetDashboardScreen.kt (Vehicle cards & Add dialog)
    ├── navigation/       # Navigation routes (Screen.kt)
    ├── settings/         # SettingsScreen.kt (PAN editor, SAF Backup & Restore)
    ├── theme/            # Thombre Red Material 3 Color & Typography
    ├── triplogger/       # TripLoggerScreen.kt (Dynamic AC / Non-AC forms)
    ├── MainActivity.kt
    ├── MainViewModel.kt
    └── VRMSApplication.kt
```

---

## 🔒 Source of Truth & Change Management

All technical requirements, edge-case mitigation rules, formulas, and schema invariants are strictly governed by the **[MASTER_SPECIFICATION.md](MASTER_SPECIFICATION.md)** file.

* Any proposed logic adjustment or requirement shift follows the **Explicit Conflict Flagging Protocol**.
* All overrides are permanently recorded in the **Change Log Audit Trail** of the Master Specification.
