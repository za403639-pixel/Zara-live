# Zara Live - Android Live Streaming App 🎙️✨

**Zara Live** is a modern, high-performance Android live streaming application built with **Kotlin**, **Jetpack Compose (Material 3)**, **CameraX**, and **Firebase**.

---

## 🌟 Key Features

1. **Grid-Based Live Streaming Room**:
   - **1 Main Host Window**: 16:9 spotlight view with real CameraX stream, audio pulse ring, and host badges.
   - **8 Guest Slots**: Interactive video slots for multi-guest broadcasting, reactions, and speaking indicators.
2. **Host Floating Control Panel**:
   - Audio Mic Mute / Unmute
   - Camera Swap (Front ⟷ Back)
   - Torch / Flashlight toggle for low-light streaming
   - Safe "Stop Stream" confirmation
3. **Interactive Audience Experience**:
   - Real-time live comments & reactions
   - Virtual gifts shop (Roses, Luxury Yacht, Crown, Supercar, Fireworks)
   - Real-time coin balances and viewer counters
4. **Authentication**:
   - Firebase Authentication with Google Sign-In and Email support
   - User profiles with streaming statistics and badges

---

## 📱 How Audience Can Install & Run the App

### Option 1: Direct APK Download (Quickest)
1. In AI Studio, click the top-right settings/menu icon and select **"Download APK"**.
2. Share the downloaded `app-debug.apk` with your friends or audience via WhatsApp, Telegram, or Google Drive.
3. On their Android phone:
   - Tap on the APK file to open it.
   - If prompted with *"Install unknown apps"*, tap **Settings** and enable **"Allow from this source"**.
   - Tap **Install** and then **Open**.
   - Grant **Camera** and **Microphone** permissions when prompted.

---

### Option 2: Push to GitHub & Auto-Build APK
1. In AI Studio, click on **Settings (top right) → "Push to GitHub"**.
2. Select or create your repository on GitHub.
3. Once pushed, the included **GitHub Actions Workflow** (`.github/workflows/build-apk.yml`) will automatically build the APK.
4. Your audience can visit the **Actions** tab or **Releases** tab on your GitHub repository and download the latest `ZaraLive-app-debug.apk` directly!

---

## 🛠️ Tech Stack & Requirements
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Camera**: AndroidX CameraX (Camera2 API)
- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 15 (API Level 36)
- **Backend**: Firebase Authentication & Firestore
