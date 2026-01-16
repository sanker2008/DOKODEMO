# DOKODEMO // V2RAY CLIENT
> **"Function over Form"** - An Industrial Neubrutalism V2Ray Client for Android.

![Badge](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg) ![Badge](https://img.shields.io/badge/Compose-Material3-4285F4.svg) ![Badge](https://img.shields.io/badge/V2Ray-Core-CCFF00.svg)

**DokoDemo** is a high-performance, design-centric proxy client built on modern Android technologies. It strictly adheres to **Neubrutalism** principles: pure black backgrounds, acid lime accents, sharp borders, and zero elevation.

---

## ⚡ Features (功能特性)

*   **Industrial UI Design**: Unique visual identity with bold typography (JetBrains Mono) and high-contrast colors.
*   **V2Ray/Xray Support**: Full support for **VLESS**, **VMESS**, **Trojan**, and **Shadowsocks** protocols via `LibXray`.
*   **Real-time Monitoring**: Oscilloscope-style traffic monitor and live speed status in notification.
*   **Advanced Routing**: 
    *   **Global**: All traffic proxied.
    *   **Bypass CN**: Direct connection for Chinese IPs/Domains.
    *   **Split Tunneling**: Per-app proxy configuration.
*   **Quick Configuration**:
    *   **QR Scanner**: Integrated CameraX scanner for quick profile addition.
    *   **Clipboard Import**: Auto-parse `vless://`, `vmess://`, `trojan://` links.
*   **Secure & Private**: Uses Android's native `VpnService` with `tun2socks` integration. No user data tracking.

---

## 🛠 Tech Stack (技术栈)

*   **Language**: Kotlin (100%)
*   **UI Framework**: Jetpack Compose + Material3
*   **Architecture**: MVVM + Clean Architecture
*   **Dependency Injection**: Hilt (Dagger)
*   **Data Persistence**: Room Database (SQLite) + DataStore (Preferences)
*   **Asynchronous**: Kotlin Coroutines + Flow
*   **Navigation**: Jetpack Navigation Compose
*   **Camera**: CameraX
*   **Core Integration**:
    *   **V2Ray Core**: `LibXray` (Go) via JNI
    *   **Tun2Socks**: `go-tun2socks`

---

## 🚀 Setup & Build (安装与构建)

### Prerequisites
*   Android Studio
*   JDK 17
*   Android SDK API 35 (min API 24)

### ⚠️ Native Libraries Setup (重要配置)

Due to repository limitations, the native V2Ray core library must be downloaded manually.

1.  **Download AAR**:
    *   Download `libv2ray.aar` from [Nicegram Xray Core Releases](https://github.com/nicegram/nicegram-xray-core/releases) or compatible `AndroidLibXrayLite` builds.
2.  **Install AAR**:
    *   Rename the file to `libv2ray.aar`.
    *   Place it in the directory: `app/libs/libv2ray.aar`.
3.  **Sync Gradle**:
    *   Click "Sync Project with Gradle Files" in Android Studio.

> **Mock Mode**: If the AAR is missing, the app will run in "Mock Mode". UI and database features will work, but **VPN connections will fail**.

### Build Command
```bash
# Build Debug APK
./gradlew assembleDebug

# Install to Device
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Project Structure

```
d:/dev/san/dd/
├── app/src/main/java/com/dokodemo/
│   ├── core/           # Core Manager (Config generation, JNI wrapper)
│   ├── data/           # Repository, Room DAO, Models
│   ├── di/             # Hilt Modules
│   ├── navigation/     # NavHost & Routes
│   ├── service/        # VpnService & Tun2Socks
│   ├── ui/
│   │   ├── components/ # Reusable Industrial UI Components
│   │   ├── screens/    # Feature Screens (Home, ServerList, Settings...)
│   │   └── theme/      # Color, Type, Theme (Neubrutalism)
│   └── ...
```

---

## 📸 Screenshots

| Home Dashboard | Server List | Config Editor |
|:---:|:---:|:---:|
| Core control & Monitor | Server selection | Profile management |

| QR Scanner | Settings | Split Tunneling |
|:---:|:---:|:---:|
| CameraX integration | Core configuration | App routing |

---

## 📄 License

This project includes code derived from V2Ray/Xray and Tun2Socks.
*   **App Code**: Apache 2.0
*   **V2Ray Core**: MPL 2.0 / GPL v3

---

**DOKODEMO** // SYSTEM_READY
