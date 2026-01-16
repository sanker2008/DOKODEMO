# DokoDemo Native Library Setup

## Required Libraries

DokoDemo requires the following native libraries for V2Ray core functionality:

### 1. libv2ray.aar (AndroidLibXrayLite)

Download from: https://github.com/nicegram/nicegram-xray-core/releases

1. Go to the releases page
2. Download `libv2ray.aar`
3. Place it in `app/libs/libv2ray.aar`

### 2. tun2socks (Optional, built-in fallback available)

The tun2socks library bridges the Android VPN TUN interface with V2Ray's SOCKS5 proxy.

If not available, the app will run in limited mode.

## GeoIP/GeoSite Data Files

For advanced routing (geo-based rules), you need:

1. Download geoip.dat: https://github.com/v2fly/geoip/releases
2. Download geosite.dat: https://github.com/v2fly/domain-list-community/releases

Place these files in `app/src/main/assets/`:
- `app/src/main/assets/geoip.dat`
- `app/src/main/assets/geosite.dat`

## Alternative: Build from Source

### libv2ray.aar

```bash
git clone https://github.com/nicegram/nicegram-xray-core.git
cd nicegram-xray-core
python3 build/main.py android
# Output: libv2ray.aar
```

### tun2socks

```bash
git clone https://github.com/nicegram/nicegram-tun2socks.git
cd nicegram-tun2socks
# Follow build instructions for Android
```

## Gradle Configuration

After placing the AAR files, the app will automatically use them.
The current `app/build.gradle.kts` includes:

```kotlin
implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
```

## Mock Mode

If native libraries are not available, DokoDemo runs in "Mock Mode":
- UI fully functional
- Connection simulation (no real VPN)
- Useful for UI development and testing
