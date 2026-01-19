---
name: Debug V2Ray Core
description: Procedures for debugging connectivity and V2Ray core issues.
---

# Debug V2Ray Core

Use this workflow to troubleshoot VPN and Core issues.

## 1. Verify Native Libraries
- Ensure `app/libs/libv2ray.aar` exists.
- Check file size (should be >10MB).

## 2. Logcat Debugging
- Filter logs by the following tags:
    - `DokoDemoVpnService`: Service lifecycle and event logs.
    - `CoreManager`: V2Ray instance management logs.
    - `GoLog`: Native logs from the V2Ray core.

```bash
adb logcat -s "DokoDemoVpnService" "CoreManager" "GoLog"
```

## 3. Validate Configuration
- The `CoreManager` generates a JSON configuration string before starting the core.
- Look for the "V2Ray Config" log entry in Logcat.
- Copy the JSON and validate it against V2Ray configuration standards if connection fails.
