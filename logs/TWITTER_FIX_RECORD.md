# Twitter (X) 账号封禁问题修复记录

## 问题背景

使用自研代理 App DokoDemo 连接 Twitter (X) 时，账号被封禁，提示"不真实行为"(Inauthentic Behavior)。使用相同的节点在 v2rayN 上从未出现问题。

---

## 问题分析

### 根本原因：流量特征泄露

Twitter 的风控系统检测到以下异常：

| 问题类型 | 具体表现 | 风险等级 |
|---------|---------|---------|
| TLS 指纹异常 | Go 语言原生 TLS 特征，非浏览器指纹 | 🔴 高 |
| DNS 泄露 | 本地 DNS 查询暴露真实位置 | 🔴 高 |
| IPv6 泄露 | IPv6 流量绕过代理，暴露真实 IP | 🔴 高 |
| 环境不一致 | 时区/语言与 IP 位置不符 | 🟡 中 |

---

## 修复过程

### 第一阶段：核心协议支持 (之前完成)

#### 1. 添加 Reality 协议支持

**文件**: `app/src/main/java/com/dokodemo/data/model/ServerProfile.kt`

**修改内容**:
```kotlin
// ─── Reality 设置 (VLESS Reality) ─────────────────────────────────────
val useReality: Boolean = false,    // 是否使用 Reality (优先于 TLS)
val realityPublicKey: String = "",  // Reality 公钥
val realityShortId: String = "",    // Reality 短 ID
val realitySpiderX: String = "",    // Reality spider 参数
val fingerprint: String = "chrome", // uTLS 指纹: chrome/firefox/safari/edge/ios/android
```

**原因**: Reality 是目前最安全的流量伪装协议，比普通 TLS 更难被检测。

---

#### 2. 完善 TLS 配置 - uTLS 指纹

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**修改内容**:
```kotlin
private fun generateStreamSettings(profile: ServerProfile, allowInsecure: Boolean): Map<String, Any> {
    return buildMap {
        // ... 网络设置 ...
        
        if (profile.useReality) {
            put("security", "reality")
            put("realitySettings", mapOf(
                "serverName" to profile.serverName.ifEmpty { profile.address },
                "publicKey" to profile.realityPublicKey,
                "shortId" to profile.realityShortId,
                "spiderX" to profile.realitySpiderX.ifEmpty { "/" },
                "fingerprint" to profile.fingerprint.ifEmpty { "chrome" }
            ))
        } else if (profile.useTls) {
            put("security", "tls")
            put("tlsSettings", mapOf(
                "serverName" to profile.serverName.ifEmpty { profile.address },
                "allowInsecure" to (profile.allowInsecure || allowInsecure),
                "fingerprint" to profile.fingerprint.ifEmpty { "chrome" }
            ))
        }
    }
}
```

**原因**: 
- 默认的 Go TLS 指纹非常明显，容易被识别为自动化脚本
- uTLS 可以模拟 Chrome/Firefox/Safari 等浏览器的 TLS 指纹
- 让流量看起来像真实的浏览器访问

---

#### 3. 添加 XTLS Vision 支持

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**修改内容**:
```kotlin
private fun generateVlessOutbound(...): Map<String, Any> {
    val effectiveFlow = when {
        profile.useReality && profile.flow.isEmpty() -> "xtls-rprx-vision"
        profile.flow.isNotEmpty() -> profile.flow
        else -> null
    }
    
    return mapOf(
        "tag" to "proxy",
        "protocol" to "vless",
        "settings" to mapOf(
            "vnext" to listOf(
                mapOf(
                    "address" to profile.address,
                    "port" to profile.port,
                    "users" to listOf(
                        buildMap<String, Any> {
                            put("id", profile.uuid)
                            put("encryption", profile.encryption.ifEmpty { "none" })
                            if (effectiveFlow != null) {
                                put("flow", effectiveFlow)  // xtls-rprx-vision
                            }
                            put("level", 0)
                        }
                    )
                )
            )
        ),
        // ...
    )
}
```

**原因**: XTLS Vision 是目前最安全的流量伪装方式，可以有效隐藏代理特征。

---

### 第二阶段：DNS 泄露修复 (本次完成)

#### 4. 强制远端 DNS 解析

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**修改前**:
```kotlin
put("dns", mapOf<String, Any>(
    "servers" to buildList {
        add(mapOf("tag" to "google", "address" to "https://dns.google/dns-query"))
        add(mapOf("tag" to "cloudflare", "address" to "https://cloudflare-dns.com/dns-query"))
        // 本地 DNS 可能泄露
    },
    "queryStrategy" to "UseIPv4",
    "disableCache" to true
))
```

**修改后**:
```kotlin
// DNS settings - 强制远端解析，防止 DNS 泄露
put("dns", mapOf<String, Any>(
    "servers" to buildList {
        add(mapOf(
            "tag" to "proxy",
            "address" to "https://dns.google/dns-query",
            "skipFallback" to true
        ))
        add(mapOf(
            "tag" to "local",
            "address" to "fakedns",
            "domains" to listOf("geosite:cn", "geosite:geolocation-cn")
        ))
    },
    "queryStrategy" to "UseIPv4",
    "disableCache" to true,
    "disableRedirect" to true
))
```

**原因**: 
- DNS 查询如果走本地，会暴露真实位置
- 使用 DoH (DNS over HTTPS) 确保加密
- 所有 DNS 查询强制通过代理服务器解析

---

#### 5. 添加 FakeDNS 配置

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**新增内容**:
```kotlin
// FakeDNS - 防止 DNS 泄露
put("fakedns", listOf(
    mapOf(
        "ipPool" to "198.18.0.0/15",
        "poolSize" to 65535
    )
))
```

**原因**: 
- FakeDNS 返回虚假 IP，实际解析在代理服务器端完成
- 完全杜绝 DNS 泄露的可能性

---

#### 6. 完善 Sniffing 配置

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**修改前**:
```kotlin
"sniffing" to mapOf(
    "enabled" to true,
    "destOverride" to listOf("http", "tls"),
    "routeOnly" to false
)
```

**修改后**:
```kotlin
"sniffing" to mapOf(
    "enabled" to true,
    "destOverride" to listOf("http", "tls", "fakedns"),
    "routeOnly" to false,
    "domainsExcluded" to listOf("courier.push.apple.com", "api.jpush.cn")
)
```

**原因**: 
- 添加 `fakedns` 到 destOverride，配合 FakeDNS 工作
- 排除某些推送服务的域名，避免影响推送功能

---

#### 7. 添加 DNS 路由规则

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**新增内容**:
```kotlin
// Routing rules
"rules" to buildList {
    // DNS 查询通过代理
    add(mapOf(
        "type" to "field",
        "inboundTag" to listOf("dns-in"),
        "outboundTag" to "proxy"
    ))
    
    // FakeDNS 返回的 IP 走代理
    add(mapOf(
        "type" to "field",
        "ip" to listOf("198.18.0.0/15"),
        "outboundTag" to "proxy"
    ))
    // ...
}
```

**原因**: 确保 DNS 流量和 FakeDNS 返回的 IP 都走代理通道。

---

### 第三阶段：IPv6 泄露修复 (本次完成)

#### 8. VPN 添加 IPv6 路由

**文件**: `app/src/main/java/com/dokodemo/service/DokoDemoVpnService.kt`

**修改前**:
```kotlin
private const val VPN_ADDRESS = "10.0.0.2"
private const val VPN_ROUTE = "0.0.0.0"
```

**修改后**:
```kotlin
private const val VPN_ADDRESS = "10.0.0.2"
private const val VPN_ADDRESS_V6 = "fd00::2"
private const val VPN_ROUTE = "0.0.0.0"
private const val VPN_ROUTE_V6 = "::"
```

**VPN Builder 修改**:
```kotlin
val vpnBuilder = Builder()
    .setSession("DokoDemo - $serverName")
    .setMtu(VPN_MTU)
    .addAddress(VPN_ADDRESS, 24)
    .addAddress(VPN_ADDRESS_V6, 64)      // 新增 IPv6 地址
    .addDnsServer(VPN_DNS_1)
    .addDnsServer(VPN_DNS_2)
    .addRoute(VPN_ROUTE, 0)
    .addRoute(VPN_ROUTE_V6, 0)           // 新增 IPv6 路由
```

**原因**: 
- 很多手机默认开启 IPv6
- 如果只代理 IPv4，IPv6 流量会直连，暴露真实 IP
- 添加 IPv6 路由后，所有 IPv6 流量也会通过 VPN

---

### 第四阶段：环境完整性 (本次完成)

#### 9. 添加安全提示

**文件**: `app/src/main/java/com/dokodemo/ui/screens/settings/SettingsScreen.kt`

**新增内容**:
```kotlin
// ─── 安全提示 ─────────────────────────────────────────────────
SettingsSection(title = stringResource(R.string.security_tips)) {
    SettingsInfoRow(
        label = stringResource(R.string.timezone_tip_title),
        value = stringResource(R.string.timezone_tip_desc)
    )
    HorizontalDivider(...)
    SettingsInfoRow(
        label = stringResource(R.string.language_tip_title),
        value = stringResource(R.string.language_tip_desc)
    )
}
```

**字符串资源** (`res/values/strings.xml`):
```xml
<string name="security_tips">安全提示</string>
<string name="timezone_tip_title">时区设置</string>
<string name="timezone_tip_desc">建议将系统时区设为与代理服务器所在地一致</string>
<string name="language_tip_title">语言设置</string>
<string name="language_tip_desc">建议将系统语言设为英语，避免被检测</string>
```

**原因**: 
- Twitter 会检测系统时区和语言是否与 IP 位置一致
- 美国IP + 北京时区 + 中文语言 = 异常
- 提醒用户手动调整系统设置

---

## 修复效果对比

| 问题 | 修复前 | 修复后 |
|------|--------|--------|
| TLS 指纹 | Go 原生特征 | 模拟 Chrome 指纹 |
| DNS 解析 | 可能本地解析 | 强制远端解析 + FakeDNS |
| IPv6 流量 | 直连泄露 | 全部通过 VPN |
| 流量嗅探 | 仅 http/tls | http/tls/fakedns |
| 环境一致性 | 无提示 | 设置页面安全提示 |

---

## 技术架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Android App                            │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │   Twitter   │    │  其他 App   │    │   系统 App   │     │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘     │
│         │                  │                  │             │
│         └──────────────────┼──────────────────┘             │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    VPN Interface                     │   │
│  │  IPv4: 10.0.0.2/24    IPv6: fd00::2/64              │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   tun2socks                          │   │
│  │  DNS: 127.0.0.1:10853 → FakeDNS                     │   │
│  └─────────────────────────┬───────────────────────────┘   │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   Xray Core                          │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │ Inbound: SOCKS5 (10808) + HTTP (10809)      │    │   │
│  │  │ Sniffing: http, tls, fakedns                │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │ DNS: DoH (dns.google) + FakeDNS             │    │   │
│  │  │ FakeDNS Pool: 198.18.0.0/15                 │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │ Outbound: VLESS + Reality/XTLS Vision       │    │   │
│  │  │ uTLS Fingerprint: chrome                    │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  └─────────────────────────┬───────────────────────────┘   │
└────────────────────────────┼───────────────────────────────┘
                             ▼
                    ┌─────────────────┐
                    │  代理服务器      │
                    │  (Reality)      │
                    └────────┬────────┘
                             ▼
                    ┌─────────────────┐
                    │  目标服务器      │
                    │  (Twitter/X)    │
                    └─────────────────┘
```

---

## 使用建议

### 1. 节点选择
- ✅ 优先使用 **Reality** 节点
- ✅ 其次使用 **VLESS + XTLS Vision**
- ⚠️ 避免使用纯 VMess 节点

### 2. 系统设置
- 📍 时区：设为代理服务器所在地时区
- 🌐 语言：建议设为英语
- 🔒 IPv6：如果服务器不支持 IPv6，建议关闭系统 IPv6

### 3. 内核版本
- 建议使用 **Xray v26.3.27** 或更高版本
- 新版本包含 Reality 和 uTLS 的重要改进

### 4. 测试建议
- 先用测试账号验证
- 确认无问题后再使用主账号
- 避免频繁切换节点

---

## 文件修改清单

| 文件路径 | 修改类型 | 说明 |
|---------|---------|------|
| `core/CoreManager.kt` | 修改 | DNS/FakeDNS/Sniffing/路由配置/mKCP FinalMask |
| `service/DokoDemoVpnService.kt` | 修改 | IPv6 路由支持 |
| `data/model/ServerProfile.kt` | 修改 | Reality 字段 |
| `core/ShareLinkParser.kt` | 修改 | Reality 链接解析 |
| `ui/screens/settings/SettingsScreen.kt` | 修改 | 安全提示 UI |
| `ui/screens/logs/LogsScreen.kt` | 修改 | 日志可选择复制 |
| `res/values/strings.xml` | 修改 | 中文字符串 |
| `res/values-en/strings.xml` | 修改 | 英文字符串 |

---

### 第五阶段：Xray v26 兼容性修复 (本次完成)

#### 10. mKCP 配置迁移到 FinalMask

**文件**: `app/src/main/java/com/dokodemo/core/CoreManager.kt`

**问题**: Xray v26.3.27 移除了旧的 mKCP `header` 和 `seed` 字段，需要使用新的 `finalmask` 格式。

**错误信息**:
```
The feature mkcp header & seed has been removed and migrated to 
finalmask/udp header-* & mkcp-original & mkcp-aes128gcm.
```

**修改前** (旧格式，已废弃):
```kotlin
"kcp" -> {
    put("kcpSettings", buildMap {
        // ...
        put("header", mapOf("type" to "dtls"))
        put("seed", "xxx")
    })
}
```

**修改后** (新格式，兼容旧配置):
```kotlin
"kcp" -> {
    put("kcpSettings", buildMap {
        put("mtu", 1100)
        put("tti", 50)
        put("uplinkCapacity", 12)
        put("downlinkCapacity", 100)
        put("congestion", true)
        put("readBufferSize", 2)
        put("writeBufferSize", 2)
    })
    
    val headerType = profile.kcpHeader.ifEmpty { "none" }
    val hasSeed = profile.kcpSeed.isNotEmpty()
    
    if (headerType != "none" || hasSeed) {
        put("finalmask", mapOf(
            "udp" to buildList {
                if (headerType != "none") {
                    add(mapOf("type" to "header-$headerType"))
                }
                if (hasSeed) {
                    add(mapOf(
                        "type" to "mkcp-aes128gcm",
                        "settings" to mapOf("seed" to profile.kcpSeed)
                    ))
                }
            }
        ))
    }
}
```

**转换规则**:

| 旧配置 | 新配置 (FinalMask) |
|--------|-------------------|
| `header.type = "dtls"` | `finalmask.udp[].type = "header-dtls"` |
| `header.type = "utp"` | `finalmask.udp[].type = "header-utp"` |
| `header.type = "srtp"` | `finalmask.udp[].type = "header-srtp"` |
| `header.type = "wechat-video"` | `finalmask.udp[].type = "header-wechat-video"` |
| `header.type = "wireguard"` | `finalmask.udp[].type = "header-wireguard"` |
| `seed = "xxx"` | `finalmask.udp[].type = "mkcp-aes128gcm"` + settings |

#### 11. 日志页面可选择复制

**文件**: `app/src/main/java/com/dokodemo/ui/screens/logs/LogsScreen.kt`

**修改内容**: 添加 `SelectionContainer` 包裹日志列表，支持长按选择复制。

```kotlin
SelectionContainer {
    LazyColumn(...) {
        items(logs) { log ->
            Text(text = log, ...)
        }
    }
}
```

---

## 版本信息

- **修复日期**: 2026-04-07
- **App 版本**: 1.0.0
- **Xray Core**: v26.3.27
- **协议支持**: VLESS, VMess, Trojan, Shadowsocks, Reality, mKCP

---

## 参考资料

- [Xray 官方文档](https://www.v2fly.org/)
- [Reality 协议说明](https://github.com/XTLS/REALITY)
- [uTLS 指纹伪装](https://github.com/refraction-networking/utls)
- [FakeDNS 原理](https://www.v2fly.org/config/dns.html#fakedns)
- [Xray v26 更新日志](https://github.com/XTLS/Xray-core/releases)
- [FinalMask 配置](https://xtls.github.io/config/transports/mkcp.html)
