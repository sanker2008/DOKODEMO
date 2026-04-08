# KCP 节点连接成功但无流量修复记录 (FinalMask 配置)

## 问题描述
节点：VMess + KCP + DTLS (SanVPN)
现象：在 v2rayNG 上正常使用，但在本应用中可以显示连接成功（VPN 图标出现），但下载流量始终为 0，且无法访问 Google。

## 环境信息
- **内核版本**: Xray-core v26.3.27
- **Lib 版本**: Lib v35

## 根本原因分析
本应用使用的 Xray 内核版本 (v26.3.27) 已经废弃了传统的 `kcpSettings` 中的 `header` 和 `seed` 字段，转而使用 `finalmask` 机制。

在修复过程中经历了三次尝试，最终确定了正确的配置规则：

1.  **分层位置**: `finalmask` 必须作为 `streamSettings` 的**顶级字段**（与 `kcpSettings` 同级），而不是嵌套在 `kcpSettings` 内部。
2.  **元素类型**: `finalmask.udp` 数组中的元素必须是**对象形式** (`{"type": "header-dtls"}`)，而不能是纯字符串。
3.  **链式结构 (Chain)**: 也是最关键的一点。mKCP 协议的基础传输和伪装层级是链式的。必须同时包含外层伪装层 (`header-dtls`) 和内层基础协议层 (`mkcp-original`)。
    - 如果只有 `header-dtls`，内核格式校验会通过并启动，但由于缺少基础传输协议层，数据无法正常解包。

## 修复方案

在 `CoreManager.kt` 的 `generateStreamSettings` 函数中，对 `kcp` 网络类型的配置进行了重构：

### 错误配置 (Legacy/Partial)
```json
"kcpSettings": {
  "header": { "type": "dtls" }
}
```
*或者*
```json
"finalmask": {
  "udp": [{ "type": "header-dtls" }] // 缺少 mkcp-original
}
```

### 正确配置
```kotlin
"kcp" -> {
    val headerType = profile.kcpHeader.ifEmpty { "none" }
    
    // 1. 基础传输参数
    put("kcpSettings", mapOf(
        "mtu" to 1350,
        "tti" to 50,
        "uplinkCapacity" to 5,
        "downlinkCapacity" to 20,
        "congestion" to false,
        "readBufferSize" to 2,
        "writeBufferSize" to 2
    ))
    
    // 2. 伪装链路 (FinalMask Chain)
    put("finalmask", mapOf(
        "udp" to buildList<Any> {
            // 第一层：外层伪装
            if (headerType != "none") {
                add(mapOf("type" to "header-$headerType"))
            }
            // 第二层：基础协议 (必须包含，否则无流量)
            add(mapOf("type" to "mkcp-original"))
            // 第三层：混淆加密 (如果有 seed)
            if (profile.kcpSeed.isNotEmpty()) {
                add(mapOf(
                    "type" to "mkcp-aes128gcm",
                    "settings" to mapOf("key" to profile.kcpSeed)
                ))
            }
        }
    ))
}
```

## 验证结果
修复后，SanVPN 节点下载速度恢复正常，Google 访问流畅。

## 注意事项
该配置逻辑高度依赖 Xray 内核版本。如果后续升级内核，需关注 Project X 官方文档中关于 `finalmask` 的变更说明。
