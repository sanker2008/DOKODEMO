# Project: DokoDemo (Android V2Ray Client)
**Version:** 1.0.0
**Type:** Android Native Application
**Target:** Android 10+
**Style:** Neubrutalism / Industrial Aesthetic (Dark Mode)

---

## 1. Project Overview
**DokoDemo** is a lightweight, high-performance V2Ray/Xray client for Android.
**Core Philosophy:** "Function over Form". The app features a strict **Neubrutalism/Industrial** design language. It rejects standard "modern" gradients and soft shadows in favor of pure black backgrounds, sharp lines, high-contrast colors, and monospace typography.

---

## 2. Design Language System (DLS)
> **Strict Adherence Required** for all UI components.

### 2.1 Color Palette (Dark Mode Default)
* **Background:** `Color(0xFF000000)` (Pure Black) - *Absolute background for all screens.*
* **Primary / Accent:** `Color(0xFFCCFF00)` (Acid Lime) - *Used for active states, highlights, main buttons, and cursor.*
* **Surface / Card:** `Color(0xFF000000)` (Pure Black) with `BorderStroke`.
* **Text Primary:** `Color(0xFFFFFFFF)` (Pure White).
* **Text Secondary:** `Color(0xFF888888)` (Grey).
* **Error:** `Color(0xFFFF0000)` (Red) or keep it Acid Lime for a monochromatic terminal look.

### 2.2 Typography
* **Headings:** Bold Sans-Serif (e.g., `Roboto-Bold` or `Oswald`). Uppercase preferred for headers.
* **Data / Body:** **Monospace is mandatory** for IPs, Ports, Latency, Logs, and Stats. (Recommended: `JetBrains Mono` or `Roboto Mono`).

### 2.3 Component Style (The "Industrial" Look)
* **Borders:** Thick, sharp borders (1dp or 2dp) for all interactive elements.
* **Corners:** Sharp (0dp) or minimal rounded (4dp). No pill shapes.
* **Shadows:** **NONE.** No elevation. Flat layers only.
* **Dividers:** Thin 1px Grey lines `Color(0xFF333333)`.

---

## 3. Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material3 implementation but heavily customized).
* **Architecture:** MVVM (Model-View-ViewModel).
* **Dependency Injection:** Hilt.
* **Navigation:** Jetpack Navigation Compose.
* **Core Logic:**
    * **V2Ray Core:** `LibXray` or `AndroidLibV2rayLite` (AAR integration).
    * **VPN Interface:** Android `VpnService` + `tun2socks`.
* **Storage:** DataStore (Preferences), Room Database (Profiles).
* **Async:** Coroutines & Flow.

---

## 4. Feature Specification & Screen Breakdown (10 Screens)

### Phase 1: The Core Loop (MVP)

#### 1. Splash Screen (`Route: Splash`)
* **Visuals:** Pure black void. Center: The "Vertical Void" or "Isometric Portal" Logo (Acid Lime).
* **Components:**
    * Logo: Animated alpha fade-in.
    * Text: "DOKODEMO" (Bold, Wide).
    * Loader: A blinking terminal cursor `_` or a raw progress bar.
    * Footer: "SYSTEM INITIALIZING..." (Monospace).
* **Logic:** Check V2Ray core extraction -> Check permissions -> Navigate to Home.

#### 2. Home Dashboard (`Route: Home`)
* **Visuals:** Industrial control panel.
* **Components:**
    * **Main Trigger:** A massive rectangular button (Center).
        * *Disconnected:* Black fill, Lime border. Text: "CONNECT".
        * *Connected:* Lime fill, Black text. Text: "DISCONNECT".
    * **Status Bar:** Top text "STATUS: [DISCONNECTED/CONNECTED]".
    * **Graph:** Bottom area. Pixelated/Line oscilloscope graph showing Up/Down speed in Lime.
    * **Current Node:** Box display showing "Server Name" and "Protocol".
* **Logic:** Bind to `VpnService`. Toggle VPN state. Listen to traffic stats flow.

#### 3. Server List (`Route: ServerList`)
* **Visuals:** High-density data list.
* **Components:**
    * **List Item:**
        * Left: Country Code box (e.g., [JP]).
        * Center: Server Name (White).
        * Right: Latency/Ping (e.g., `35ms`) in **Large Lime Monospace**.
    * **Selection:** Active item gets solid Acid Lime background.
    * **Ping All:** Floating Action Button (Square) to test latency.
* **Logic:** Click to select profile. Click FAB to concurrent ping all nodes.

### Phase 2: Configuration & Management

#### 4. Add Profile Modal (`Route: AddProfileSheet`)
* **Visuals:** Bottom Sheet. Solid Black.
* **Components:** 3 Stacked Blocky Buttons.
    * [ SCAN QR CODE ] (Camera Icon)
    * [ PASTE CLIPBOARD ] (Clipboard Icon)
    * [ MANUAL ENTRY ] (Edit Icon)
* **Logic:** Route to Scanner, or parse clipboard content (vless://, vmess://), or Route to Editor.

#### 5. QR Scanner (`Route: QrScanner`)
* **Visuals:** Terminator HUD / Sci-fi Viewfinder.
* **Components:**
    * Camera Preview (CameraX).
    * Overlay: Sharp Acid Lime square bracket `[   ]`.
    * Corner Text: "TARGET ACQUIRED", "SCANNING...".
    * Buttons: "FLASH", "GALLERY" (Wireframe style).
* **Logic:** Scan code -> Parse config -> Save to DB -> Toast "Profile Added".

#### 6. Subscription Manager (`Route: Subscriptions`)
* **Visuals:** Inventory list.
* **Components:**
    * List of URLs.
    * Detail: "Remarks" + Truncated URL.
    * Action: Square "UPDATE" button on the right of each row.
    * Footer: "ADD SUBSCRIPTION" button.
* **Logic:** Fetch Base64/SIP002 data -> Parse -> Update DB nodes.

#### 7. Config Editor (`Route: ConfigEditor`)
* **Visuals:** Data entry terminal.
* **Components:**
    * Fields: Address, Port, UUID, Flow, Encryption.
    * Style: Label *above* input (Grey Monospace). Input box is black with Lime underline when focused.
    * Action: Full-width "SAVE CONFIG" button (Lime background).
* **Logic:** Validation (Port is number, UUID is valid). Save to Room DB.

### Phase 3: Advanced & Debug

#### 8. Settings Hub (`Route: Settings`)
* **Visuals:** Control panel switches.
* **Components:**
    * Headers: `[ NETWORK ]`, `[ BEHAVIOR ]`.
    * **Custom Toggle:** Square box. Empty = OFF. Filled Lime = ON.
    * Items: "Allow LAN", "Sniffing", "Routing Mode", "Dark Mode".
* **Logic:** Save preferences to DataStore.

#### 9. Split Tunneling / App Proxy (`Route: SplitTunneling`)
* **Visuals:** Checklist.
* **Components:**
    * Search Bar: Rectangular border.
    * List: App Icon + App Name + Package Name (small grey).
    * Checkbox: Sharp square. Checked = Lime Fill.
* **Logic:** Load installed apps. Save selected package names to config.

#### 10. System Logs (`Route: Logs`)
* **Visuals:** Hacker Matrix Terminal.
* **Components:**
    * Background: Pure Black.
    * Text: 100% Monospace.
    * Colors: Info (White), Error (Lime), Time (Grey).
    * Header: "TERMINAL OUTPUT".
* **Logic:** Stream logs from `LibXray` callback. Auto-scroll to bottom.

---

## 5. Asset Generation Prompts (Reference)

**Global Style Prompt:**
> "Mobile app UI design for 'DokoDemo' VPN client. Style is strictly **Neubrutalism / Industrial aesthetic**. **Background is Pure Black (#000000).** The ONLY accent color is high-contrast sharp **Acid Lime (#CCFF00)**. No gradients, no soft shadows. Typography is Monospace. Utilitarian feel."

**Logo Prompt (The Isometric Gateway):**
> "App icon for 'DokoDemo'. An isometric view of a heavy, industrial sci-fi gateway structure. The thick metal frame is outlined with hard Acid Lime neon light (#CCFF00). The gate is open, showing a dark tunnel receding into the pure black background. Strong perspective, hard edges. Rounded square shape."

---

## 6. Implementation Roadmap

1.  **Project Setup:** Initialize Android Studio, setup Gradle (Hilt, Compose, Room).
2.  **UI Foundation:** Create `Color.kt` and `Theme.kt` enforcing the Black/Lime palette. Build reusable `IndustrialButton`, `IndustrialInput`, `IndustrialCard`.
3.  **Navigation Skeleton:** Create the NavHost and empty screens for all 10 routes.
4.  **Core Integration:** Import `LibXray`. Implement `VpnService` start/stop logic.
5.  **Data Layer:** Define Room Entities (`ServerProfile`, `Subscription`).
6.  **Screen Logic (Iterative):**
    * Build Home (UI + VPN toggle).
    * Build Server List (CRUD).
    * Build Config Editor.
7.  **Advanced Features:** Implement Subscriptions and QR Scanning.
8.  **Optimization:** ProGuard rules, memory leak checks (VPN service), battery optimization.