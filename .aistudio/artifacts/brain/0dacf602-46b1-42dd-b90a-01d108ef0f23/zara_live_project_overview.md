# 🎙️ Zara Live — Project Overview & Feature Guide

Welcome to **Zara Live**, a full-featured live video streaming & guest cypher room Android application.

---

## 📱 App Highlights & Architecture

### 1. 🎛️ 8-Guest + 1-Host Seat Stage (`StreamSeatGridComponent.kt`)
- **Spotlight Mode (1+8)**: 1 large host screen at the top, and 8 numbered guest tiles (`#1` to `#8`) in an interactive grid below.
- **Equal Grid Mode (3x3)**: 9 equal-sized tiles (1 Host + 8 Guests) arranged in a balanced matrix.
- **Seat States**:
  - `Empty`: Animated inviting slot showing "+" and seat number. Tap to request that specific seat.
  - `Occupied`: Live camera preview or avatar with real-time speaking equalizer pulse, mute indicator, and hand-raise badges.
  - `Host Controls`: Host can tap any seat to mute/unmute, turn video on/off, or kick/vacate the guest.

### 2. 🙋‍♂️ Stream Join Request Workflow
- **Audience Request**: Tap **"Request to Join"** or tap any vacant seat tile.
  - Choose preferred seat (Seat 1–8 or Any Seat).
  - Pre-configure Camera & Microphone states.
  - Add optional custom introduction note.
- **Host Approval Queue**:
  - Host sees real-time badge with pending request count.
  - Bottom sheet dialog allows host to **Approve & Assign Seat** or **Decline**.

### 3. 📹 Camera & Broadcast Studio (`BroadcastStudioScreen.kt`)
- Multi-tab studio:
  - **Tab 1: Camera & Live Preview**: Real CameraX video feed with interactive control panel (Mute audio, swap front/back camera, torch toggle, stop broadcast).
  - **Tab 2: 8+1 Guest Stage**: Full interactive seat grid and guest moderation.
  - **Tab 3: Multi-Stream Targets**: Stream to YouTube, Twitch, TikTok, Facebook Live.

### 4. 💬 Live Chat & Interactive Gifts (`LiveChatOverlay.kt`)
- Real-time animated chat overlay with emoji bubbles, viewer count, and tip/gift animations.

### 5. 🔐 Authentication & Profile Sync
- Email/Password and Google Sign-In options.
- User profile data model syncing with Firebase Realtime Database / Firestore.

---

## 🚀 How to Run & View the App
1. **Streaming Android Emulator**: The Android emulator in the preview tab automatically compiles and runs the APK. If the emulator is reloading, give it a few seconds to boot up and launch Zara Live.
2. **Project Code**: All source files are located in `/app/src/main/java/com/example/`.
3. **Export & APK**: You can export the complete project as a ZIP or build the release APK directly via the top-right settings menu.
