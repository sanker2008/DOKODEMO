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

---

## 🛠️ Usage Guide (使用指南)

### 1. Import Profile (导入节点)
DokoDemo supports multiple ways to import server configurations:

*   **QR Scan**: Tap the **[SCAN]** button on the home dashboard to scan a VLESS/VMESS/Trojan QR code.
*   **Clipboard**: Copy a `vmess://`, `vless://`, or `trojan://` link and tap **[IMPORT]**.
*   **Manual**: Navigate to **Server List** -> **Add (+)** -> **Manual Input**.

### 2. Connect (启动连接)
*   On the **Home Dashboard**, tap the large **[CONNECT]** button.
*   **First Run**: You will be prompted to grant **VPN Permission**. Accept to proceed.
*   **Status**:
    *   **IDLE**: Disconnected.
    *   **CONNECTING**: Handshaking...
    *   **CONNECTED**: VPN tunnel active. Traffic monitoring curve will activate.

### 3. Split Tunneling (分应用代理)
*   Go to **Settings** -> **Split Tunneling**.
*   **Mode**:
    *   **Proxy All**: All apps go through VPN.
    *   **Bypass Selected**: Selected apps bypass VPN (Direct).
    *   **Proxy Selected**: Only selected apps use VPN.
*   Toggle switches for installed apps to configure their routing.

### 4. System Logs (日志调试)
*   Tap the **Terminal Icon** (Bottom Right) to open the System Logs console.
*   View real-time V2Ray Core logs for troubleshooting connection issues.

---

## ⚠️ Troubleshooting (常见问题)

**Q: Failed to load V2Ray Core?**
A: Ensure you have successfully set up the native libraries (see **Setup & Build**). The app requires `libv2ray.aar` and `geoip/geosite` assets.

**Q: Connection Timeout?**
A: Check your server configuration. Verify that the server time matches your device time (V2Ray is time-sensitive).

**Q: "Mock Mode" Warning?**
A: This means `libv2ray.aar` was not found during build. The app is running in UI-only mode. Please download and install the AAR.

---

> **Note**: This project is for educational and research purposes only. Please comply with local laws and regulations.
