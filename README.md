# Stopify

<p align="center">
  <img src="logo.png" alt="Stopify Logo" width="160" height="160" />
</p>

<p align="center">
  <strong>Native Android Music Player & Spotify Web DRM Client with Full Free Account Playback</strong>
</p>

<p align="center">
  <strong>Crafted by Shashwat</strong>
</p>

---

# ⚠️ FOR EDUCATIONAL & RESEARCH PURPOSES ONLY ⚠️

> **CRITICAL LEGAL NOTICE & DISCLAIMER**
> 
> **THIS PROJECT WAS CREATED STRICTLY FOR EDUCATIONAL, ACADEMIC, AND REVERSE ENGINEERING RESEARCH PURPOSES.**
> 
> All trademarks, service marks, trade names, product names, and logos appearing in this project are the property of their respective owners (Spotify AB). This software is an independent research project developed to demonstrate modern Android architecture, WebSocket event buses, and WebPlayer protocol analysis. It is not affiliated with, endorsed by, or sponsored by Spotify AB.
> 
> The author does not encourage or condone unauthorized access, copyright infringement, or violation of Spotify's Terms of Service. Use this repository at your own discretion.

---

## 🌟 Introduction & The Breakthrough

**Stopify** is—to the best of my knowledge—the **first client on the internet** to successfully reverse engineer Spotify's modern Web Player DRM and streaming pipeline to enable **direct on-demand audio playback for Spotify Free accounts** natively on Android.

Traditional open-source Spotify clients (such as librespot, Spotube, etc.) rely on either:
1. **Spotify Connect protocols (`spirc`)**, which strictly demand a paid **Spotify Premium** account; or
2. **YouTube scraping fallbacks**, which do not stream audio from Spotify's servers directly.

**Stopify solves this fundamentally** by speaking Spotify's authentic Web Player protocols directly. By masquerading as an authorized browser instance and tapping into Spotify's Dealer WebSocket bus, Stopify streams interactive audio directly from Spotify's content delivery network (CDN) for free tier accounts.

---

## ✨ Features

### 🎧 Core Playback & Audio
- **Direct Spotify CDN Streaming**: Interactive, on-demand audio stream resolution directly from Spotify's CDN servers for free-tier accounts.
- **AndroidX Media3 ExoPlayer Engine**: Seamless foreground and background playback with full MediaSession integration and notification controls.
- **Playback Reliability & Auto-Retry**: Built-in playback watchdog, single in-flight resolution locks, and silent background token refresh.
- **Offline Download Manager**: Download tracks with metadata and high-res cover art for local offline playback.
- **Smart Autoqueue**: Persistent playback queue store (`PlaybackQueueStore`) with automatic recommendation autoqueuing when playlist ends.
- **Synchronized Lyrics**: Line-by-line synchronized lyrics powered by Spotify's internal lyrics provider with an expandable full-screen viewer.

### 🌐 Spotify Connect Sync & Remote Control
- **Live Cluster Synchronization**: Listens on the Dealer WebSocket for state-machine broadcasts from any active Spotify device.
- **Remote Playback Control**: Play, pause, skip, and seek other Spotify Connect devices (desktop, web player, smart speakers) straight from Stopify.
- **Device Picker Bottom Sheet**: Seamlessly transfer playback between devices via the in-player device menu.

### 🎨 Design: "Aura × Spectrum" (Prism UI)
- **Floating Glass Dock**: Decoupled floating mini-player card and ultra-compact navigation pill.
- **Glass Surfaces**: Configurable surface rendering modes (`Solid`, `Blurred`, `Liquid` with backdrop blur and refracting lens rim).
- **Split-Light Gradients**: Accent glow on scrubbers, active tab indicators, and live playing rings.
- **Outfit Variable Typography**: Clean, contemporary type hierarchy designed for modern high-refresh mobile displays.
- **Full Player Experience**: Ambient background artwork blur, soft 22dp/28dp elevation shadows, swipe-to-skip gestures, and quick tool capsule.
- **Dedicated Recents & Merged History**: Persistent listening history ledger and fast resume cards.

### ⚡ Performance & Size Optimization
- **Lightweight ~5 MB Footprint**: Stripped monolithic icon packs in favor of modular vector icons.
- **Full R8 Optimization**: Release builds utilize R8 code and resource shrinking for rapid startup and minimal memory footprint.
- **Adaptive WebP App Icons**: Modern adaptive launcher icon set with themed monochrome and rounded variants.

---

## 🔬 How It Was Done: Reverse Engineering Spotify's Web Player

To achieve full audio playback on a free Spotify account, the following internal Spotify protocols and subsystems were analyzed and implemented from scratch in pure Kotlin:

### 1. Pathfinder GraphQL API (v2)
Spotify's official search and metadata queries use an internal GraphQL gateway (`api-partner.spotify.com/pathfinder/v2/query`).
- **Persisted Query Hashing**: Spotify requires unique SHA-256 operation hashes (e.g., `dd78eaff943eba629ed70ee...` for `searchTopResultsList`).
- **Dynamic Scopes**: Queries require an OAuth Bearer token (`BQ...`) with specific web player audience claims.

### 2. The Dealer WebSocket Bus (`wss://gae2-dealer.g2.spotify.com`)
When a Web Player session opens, Spotify connects to a real-time messaging pipeline known as the **Dealer**.
- Stopify establishes an active persistent WebSocket connection using OkHttp.
- Upon handshake, Spotify sends a message containing the unique `Spotify-Connection-Id`.
- Stopify captures and stores this connection ID asynchronously.

### 3. Web Player Device Registration
Before Spotify's cluster routes audio commands, the client must register as a virtual Web Player device with the playback backend:
- **Endpoint**: `https://gae2-spclient.spotify.com/track-playback/v1/devices`
- **Payload**: Supplies the client's `device_id`, platform identifier (`web_player osx 10.15.7;chrome 155.0.0.0;desktop`), capabilities flags (`supports_file_media_type`, `manifest_formats: [file_ids_mp4, file_ids_mp4_dual]`), and the live `Spotify-Connection-Id`.
- **Headers**: Authenticated with the user's `authorization: Bearer BQ...` token and dynamic `client-token`.

### 4. Connect-State Playback Commands & Manifest Resolution
Once registered, Stopify issues an internal play command:
- **Endpoint**: `https://gae2-spclient.spotify.com/connect-state/v1/player/command/from/{device_id}/to/{device_id}`
- **Dealer Cluster Response**: Instead of responding synchronously over HTTP, Spotify pushes a state-machine update over the persistent **Dealer WebSocket**.
- **Manifest Extraction**: Stopify listens on the WebSocket for `state_machine.tracks` containing `manifest.file_ids_mp4` (the 40-character hex identifier of the raw audio file).

### 5. Storage CDN Audio Resolution
With the resolved `file_id`, Stopify calls Spotify's storage resolution service:
- **Endpoint**: `https://gae2-spclient.spotify.com/storage-resolve/v2/files/audio/interactive/10/{file_id}?version=10000000&product=9&platform=39&alt=json`
- **Output**: Returns signed, direct CDN URLs (`audio-ak.spotifycdn.com`) with expiration and HMAC tokens, which are streamed directly via Android Media3 ExoPlayer.

### 6. Seamless In-App Session & Token Harvesting
Because raw HTTP calls to `/get_access_token` are protected by Spotify's edge bot detection (Akamai / Cloudflare), Stopify features an embedded Chromium `WebView` authentication engine:
- Pre-seeds active session cookies (`sp_dc`, `sp_key`, `sp_t`).
- Intercepts authentic `authorization: Bearer BQ...` headers and `client-token` in real-time from outgoing requests.
- Executes safe in-origin token fetching via JavaScript bridge (`StopifyBridge`) directly inside Spotify's authentic browser context.

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- Android SDK 35+ (compiles with Android 37 / Vanilla Ice Cream)
- Java 17+

### Build from Terminal
```bash
# Clone the repository
git clone https://github.com/myself-tsuki/Stopify.git
cd Stopify

# Build Debug APK
./gradlew assembleDebug

# Build Release APK (R8 Optimized)
./gradlew assembleRelease

# Install on connected device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Developed by <strong>Shashwat</strong> • Educational Research Project
</p>
