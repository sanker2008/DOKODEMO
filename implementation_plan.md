# DokoDemo — 完整开发规划

> 本文档面向技术零基础读者，包含技术原理讲解、页面功能设计、代码保留决策。

---

## 第一章：核心技术原理（先看这里）

### 1.1 什么是代理，和 VPN 有什么区别？

**代理（Proxy）** 是一个"中间人"，你发送的网络请求不直接到目标服务器，而是先到代理服务器，由代理服务器代替你访问：

```
你的手机 ──→ 代理服务器（104.238.140.227）──→ TikTok 服务器
```

**VPN（Virtual Private Network）** 的工作原理类似，但它是在系统层面建立一条"虚拟隧道"，让 **手机上所有 App 的流量** 都自动走隧道，不需要每个 App 单独配置。

DokoDemo 使用的是 **VPN 技术做流量劫持 + 代理协议做数据传输** 的组合方案。

---

### 1.2 为什么要用 VMess/VLESS/Shadowsocks 这些协议？

普通代理（如 SOCKS5/HTTP）的流量特征很明显，防火墙能轻易识别并封锁。

VMess、VLESS、Trojan、Shadowsocks 这些协议专门设计用来 **伪装成普通的网页流量**：

| 协议 | 伪装方式 | 特点 |
|------|---------|------|
| **VMess** | 加密后走 TCP/KCP/WebSocket | V2Ray 原创，灵活，你现在用的协议 |
| **VLESS** | VMess 轻量版，去掉冗余加密 | 更快，配合 Reality 极难被识别 |
| **Shadowsocks** | 对称加密，流量随机化 | 简单、稳定、广泛兼容 |
| **Trojan** | 完全模拟 HTTPS 流量 | 防火墙几乎无法区分 |
| **Hysteria2** | 基于 QUIC（即 HTTP/3）UDP 传输 | 在高丢包网络下极快 |

---

### 1.3 什么是 KCP + DTLS？（你的节点用的就是这个）

**KCP** 是一种基于 **UDP** 的传输协议（普通网页用 TCP，KCP 用 UDP）。

- TCP 像挂号信：丢了要补发，稳定但慢
- UDP 像普通明信片：丢了不管，但快
- KCP 在 UDP 基础上加了重传机制，兼顾速度和可靠性

**DTLS 伪装** 是 KCP 的伪装模式，让 KCP 的 UDP 流量看起来像 DTLS 协议（一种合法的加密协议），防止防火墙识别。

你的节点配置：`VMess + KCP 传输 + DTLS 伪装`，这是一个以速度为优先的组合。

---

### 1.4 Xray-core 是什么？

**Xray-core** 是用 Go 语言编写的开源代理核心，由社区在 V2Ray 基础上二次开发。

它的作用是：
1. 接收来自 tun2socks 的流量（本地 SOCKS5 端口 10808）
2. 按照节点配置（VMess/KCP/DTLS…），对流量加密+伪装
3. 将处理好的数据包发送到真实的代理服务器

在 Android 上，它被编译成 `libv2ray.aar`（原生库文件），通过 JNI（Java 调用 C/Go 的桥接技术）由 Kotlin 代码控制启停。

---

### 1.5 Android 全局代理的实现原理（重点）

Android 系统不允许普通 App 直接"监听"所有网络流量，但提供了一个官方接口 [VpnService](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/service/DokoDemoVpnService.kt#38-456)，专门用于 VPN 类应用。

整个流量链路如下（从你的手机到 TikTok）：

```
Step 1：手机上所有 App 产生网络流量
         ↓
Step 2：VpnService 创建一个"虚拟网卡"（TUN 设备）
        所有流量被系统自动导入这个虚拟网卡
         ↓
Step 3：tun2socks 读取虚拟网卡的数据
        将 TCP/UDP 包转换成 SOCKS5 协议格式
         ↓
Step 4：Xray-core 在本地监听 SOCKS5 端口（:10808）
        接到流量后，用 VMess+KCP+DTLS 加密封装
         ↓
Step 5：加密后的数据包发往你的代理服务器
        （104.238.140.227:8080）
         ↓
Step 6：代理服务器解密，帮你访问 TikTok
         ↓
Step 7：响应数据原路返回到手机
```

**四个核心组件**：

| 组件 | 角色 | 对应代码 |
|------|------|---------|
| [VpnService](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/service/DokoDemoVpnService.kt#38-456) | 创建虚拟网卡，劫持所有流量 | [DokoDemoVpnService.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/service/DokoDemoVpnService.kt) |
| `tun2socks` | 把虚拟网卡流量转成 SOCKS5 | [Tun2socks.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/go/tun2socks/Tun2socks.kt)（JNI） |
| `Xray-core` | 加密+代理核心 | [CoreManager.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/CoreManager.kt) + `libv2ray.aar` |
| `geoip/geosite` | IP 和域名分类数据库，用于路由判断 | `assets/geoip.dat` / `geosite.dat` |

---

### 1.6 订阅是什么？

订阅（Subscription）是一个 URL 链接，指向一个文本文件，文件里包含多个节点的配置信息。

```
订阅 URL: https://example.com/sub?token=abc
下载内容（Base64 编码后）：
  vmess://eyJhZGQiOiIxMDQuMjM4...  ← 节点1
  vless://uuid@server:443?...       ← 节点2
  trojan://pass@server:443#name     ← 节点3
```

App 定期拉取这个 URL，解析其中的节点，自动更新节点列表。这样当节点失效时，提供者只需更新文件，你不用手动重新添加。

---

## 第二章：UI 设计决策

### DOKODEMO 原 UI（工业终端风）→ 存在问题

原设计：纯黑背景 + 酸橙绿 + 全大写等宽字体，像一个黑客终端。  
问题：**视觉信息密度过高，普通用户难以快速找到核心操作**。

### 新 UI 方向：现代、通透、干净且温暖

| 设计元素 | 选择 | 原因 |
|---------|------|------|
| 背景色 | `#F0F4F7` (Light) / `#121212` (Dark) | 干净舒适，适配深浅色模式 |
| 主操作色 | Soft Blue `#A0C4E3` | 温和、易于接受 |
| 文本/图标 | Slate `#607D8B` | 清晰且不刺眼 |
| 状态高亮 | Soft Mint Green `#B7D5C7` | 用于连接成功状态及呼吸灯动画，表示安全 |
| 卡片/组件 | 大圆角 (16dp+)，轻微 Glassmorphism | 层次清晰，现代感强 |
| 字体正文 | 系统默认 Sans-serif (支持大小调节) | 兼顾多语言阅读体验 |
| 连接按钮 | 大圆形，连接后带呼吸光环动画 | 核心操作，反馈明确 |

---

## 第三章：页面设计（共 7 个主屏 + 2 个弹层）

---

### 页面 1：主页 Dashboard

**这是用户每次打开 App 看到的第一个界面，也是最核心的操作界面。**

```
┌─────────────────────────────┐
│  DokoDemo                [路由模式] │
│─────────────────────────────│
│  状态：未连接                │
│                              │
│       ╭──────────╮           │
│       │    🔵    │           │  ← 大圆形按钮
│       │   连接   │           │  ← 点击连接/断开
│       ╰──────────╯           │
│                              │
│  当前节点: SanVPN  [切换 >]  │
│  协议: VMess | KCP | 延迟:-- │
│─────────────────────────────│
│  ↑ 0 B/s    ↓ 0 B/s         │
│  [流量折线图]                 │
│─────────────────────────────│
│  🏠主页  📋节点  📡订阅  ⚙️设置│
└─────────────────────────────┘
```

**操作和功能**：
- **点击大圆形按钮**：连接 VPN（启动 VpnService → 启动 Xray-core → 建立 tun2socks）
- **点击"当前节点"区域**：跳转到节点列表选择节点
- **流量折线图**：实时显示上传/下载速率，每秒刷新一次
- **首次使用无节点时**：圆形按钮变灰不可点，提示"请先在节点页面添加节点"

---

### 页面 2：节点列表

**管理所有代理节点的地方，相当于通讯录。**

```
┌─────────────────────────────┐
│  节点列表  [⚡延迟测试] [+添加]│
│─────────────────────────────│
│  [全部] [默认分组] [机场A]   │  ← 分组标签，可左右滑动
│─────────────────────────────│
│  🔵 SanVPN                  │
│     VMess | 104.238.140.227 │
│     ████ 35ms    ← 信号格    │
│─────────────────────────────│
│  ○  节点2                   │
│     VLESS | server2.com     │
│     ██▒▒ 120ms              │
│─────────────────────────────│
│  ○  节点3 ...               │
│─────────────────────────────│
│           [+]               │  ← 浮动添加按钮
└─────────────────────────────┘
```

**操作和功能**：
- **点击节点**：选中该节点为当前活跃节点（返回主页后连接此节点）
- **长按节点**：弹出菜单 → 编辑 / 删除 / 移入分组 / 复制节点链接
- **点击 [+]**：弹出添加方式选择（手动填写 / 粘贴链接 / 扫码）
- **点击 [⚡延迟测试]**：并发对所有节点 ping 测试，更新延迟显示
- **分组标签**：筛选显示对应分组的节点

---

### 页面 3：添加节点（手动填写）

**手动填写节点参数（比如把你截图里的 VMess 节点录入 App）。**

三种进入方式：URL 粘贴解析 / QR 扫码自动填充 / 纯手动

```
┌─────────────────────────────┐
│  ← 取消    添加节点    保存  │
│─────────────────────────────│
│  协议:  [VMess] [VLESS] [SS] [Trojan]│
│─────────────────────────────│
│  别名（节点名称）            │
│  [SanVPN               ]    │
│                              │
│  服务器地址                  │
│  [104.238.140.227      ]    │
│                              │
│  端口                        │
│  [8080                 ]    │
│                              │
│  用户 ID (UUID)             │
│  [243ed98b-172a-...    ]    │
│                              │
│  传输协议                    │
│  [TCP ▾] 或 [KCP ▾]         │
│                              │
│  ▼ KCP 伪装类型（KCP时显示）  │
│  [DTLS ▾]                   │
│                              │
│  加密方式                    │
│  [auto ▾]                   │
│                              │
│  [         保 存         ]   │
└─────────────────────────────┘
```

**操作和功能**：
- **协议切换**：点击协议 Tab，下方表单字段随之变化（VMess 显示 UUID，Trojan 显示密码，SS 显示密码+加密方式）
- **传输协议 KCP**：选中后出现"伪装类型"下拉（none/dtls/utp/srtp/wechat-video）
- **粘贴 vmess:// 链接**：弹出"是否解析链接"提示，点确认自动填充所有字段
- **保存验证**：地址为空、端口不是数字 → 实时报错提示

---

### 页面 4：订阅管理

**通过一个 URL 批量导入和更新节点。**

```
┌─────────────────────────────┐
│  订阅管理        [全部更新]  │
│─────────────────────────────│
│  机场A                      │
│  url: sub.airport.com/...   │
│  节点数：15  上次更新：1小时前│
│                    [↻更新]  │
│─────────────────────────────│
│  机场B                      │
│  url: other.sub.com/...     │
│  节点数：8   上次更新：昨天   │
│                    [↻更新]  │
│─────────────────────────────│
│                              │
│   [  +  添加订阅链接  ]     │
└─────────────────────────────┘
```

**[添加订阅] 弹层**：
```
订阅名称: [机场A          ]
订阅 URL: [https://...    ]
导入到分组: [默认分组 ▾   ]
          [   确认导入   ]
```

**操作和功能**：
- **添加订阅**：输入 URL → 立即拉取并解析节点 → 存入数据库
- **点击 [↻更新]**：重新拉取该订阅并同步最新节点（新节点追加，旧节点标记过期）
- **点击 [全部更新]**：并发更新所有订阅
- **长按订阅卡片**：删除 / 重命名

---

### 页面 5：设置

**全局配置选项。**

```
┌─────────────────────────────┐
│  ← 返回        设置         │
│─────────────────────────────│
│  [ 代理路由 ]               │
│  路由模式        [全局 ▾]   │  ← 全局 / 绕过CN / 仅代理选中
│─────────────────────────────│
│  [ 代理规则 ]               │
│  分应用代理      [进入 →]   │
│  广告过滤        [○关]      │
│─────────────────────────────│
│  [ 高级设置 ]               │
│  Mux 多路复用    [○关]      │
│  允许不安全证书   [○关]      │
│  DNS 服务器      [1.1.1.1]  │
│  开机自启动       [○关]      │
│─────────────────────────────│
│  [ 应用 ]                   │
│  主题            [深色 ▾]   │
│  查看日志        [进入 →]   │
│  核心版本        1.8.x      │
│  App 版本        1.0.0      │
│  清除所有数据    [危险操作]  │
└─────────────────────────────┘
```

**路由模式说明（对用户显示中文解释）**：
- **全局**：手机上所有流量都走代理，包括 TikTok —— **推荐，最简单**
- **绕过国内**：国内网站直连，境外网站走代理
- **仅代理选中应用**：只有你勾选的 App 走代理（在"分应用代理"里选）

---

### 页面 6：分应用代理

```
┌─────────────────────────────┐
│  ←       分应用代理          │
│─────────────────────────────│
│  模式: ○全部代理  ●仅代理选中  ○排除选中│
│─────────────────────────────│
│  🔍 搜索应用                 │
│─────────────────────────────│
│  🎵 TikTok    ●          │
│  🌐 Chrome    ●          │
│  📱 微信      ○          │
│  📱 支付宝    ○          │
│  ...                        │
└─────────────────────────────┘
```

**操作**：每个 App 独立开关，选中 = 走代理，未选中 = 直连

---

### 页面 7：系统日志

**调试用，显示 Xray-core 的实时运行日志，出错时可以看这里找原因。**

```
┌─────────────────────────────┐
│  ← 返回    日志     [清空]  │
│─────────────────────────────│
│  2026/03/21 01:20:00 [Info] │
│  VPN Started                │
│  2026/03/21 01:20:01 [Info] │
│  Connected to 104.238.140.. │
│  2026/03/21 01:20:05 [Warn] │
│  ...                        │
└─────────────────────────────┘
```

---

### 弹层：分组管理

从节点列表右上角进入：

```
┌─────────────────────────────┐
│  分组管理           [+新建]  │
│─────────────────────────────│
│  📁 默认分组        3个节点 │
│  📁 机场A           15个节点│
│     绑定订阅：sub.airport..  │
│  📁 机场B           8个节点 │
│─────────────────────────────│
│  长按分组 → 重命名 / 删除    │
└─────────────────────────────┘
```

---

## 第四章：DOKODEMO 代码处置（每个文件的去留）

### ✅ 完整保留（不动）

这些文件是应用的"发动机"，和 UI 风格无关：

| 文件 | 功能说明 |
|------|---------|
| [CoreManager.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/CoreManager.kt) | 控制 Xray-core 的启停，生成 JSON 配置 |
| [DokoDemoVpnService.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/service/DokoDemoVpnService.kt) | Android VpnService 实现，建立 TUN 隧道 |
| [VpnController.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/service/VpnController.kt) | VPN 状态机控制器 |
| [ServerPinger.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/ServerPinger.kt) | 并发 ping 测速 |
| [AppDatabase.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/AppDatabase.kt) | Room 数据库定义 |
| [ServerProfile.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/model/ServerProfile.kt) | 节点数据模型（Room 实体） |
| [Subscription.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/model/Subscription.kt) | 订阅数据模型（追加 groupId 字段） |
| [ServerDao.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/dao/ServerDao.kt) | 节点的增删改查数据库操作 |
| [SubscriptionDao.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/dao/SubscriptionDao.kt) | 订阅的数据库操作 |
| [AppPreferences.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/preferences/AppPreferences.kt) | 设置项持久化（DataStore）|
| [ServerRepository.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/repository/ServerRepository.kt) | 节点仓库，连接 DAO |
| [QrCodeAnalyzer.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/qrscanner/QrCodeAnalyzer.kt) | ZXing QR 识别逻辑 |
| [DokoApplication.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/DokoApplication.kt) | Application 入口 |
| [DatabaseModule.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/di/DatabaseModule.kt) | Hilt 依赖注入模块 |
| [ShareLinkParser.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/ShareLinkParser.kt) | 节点链接解析（追加 ss://）|
| [Libv2ray.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/libv2ray/Libv2ray.kt) | JNI 桥接（调用 libv2ray.aar）|
| [Tun2socks.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/go/tun2socks/Tun2socks.kt) | JNI 桥接（调用 tun2socks.so）|

### ✅ 保留逻辑，重写 UI 样式

这些文件的业务逻辑保留，只改 Compose UI 代码：

| 文件 | 保留内容 | 改动内容 |
|------|---------|---------|
| [HomeScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/home/HomeScreen.kt) | 流量统计 + VPN 状态绑定 | 全部 UI 布局，新圆形按钮 |
| [HomeViewModel.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/home/HomeViewModel.kt) | 连接状态管理 | 不改 |
| [ServerListScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/serverlist/ServerListScreen.kt) | 搜索 + 节点列表 | UI 样式 + 加分组 Tab |
| [ServerListViewModel.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/serverlist/ServerListViewModel.kt) | 节点增删查 | 加分组筛选逻辑 |
| [ConfigEditorScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/configeditor/ConfigEditorScreen.kt) | URI 解析 + 保存 | 全部 UI + 加 KCP/SS 字段 |
| [ConfigEditorViewModel.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/configeditor/ConfigEditorViewModel.kt) | 表单验证 | 加 KCP/SS 字段处理 |
| [QrScannerScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/qrscanner/QrScannerScreen.kt) | CameraX 扫码实现 | 去掉终端风 HUD |
| [SettingsScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/settings/SettingsScreen.kt) | DataStore 绑定 | UI 样式 + 加路由模式切换 |
| [SplitTunnelingScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/splittunneling/SplitTunnelingScreen.kt) | 已安装应用遍历 | UI 样式 |
| [DokoNavHost.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/navigation/DokoNavHost.kt) | 路由结构 | 追加新页面路由 |

### ❌ 删除（不再使用）

| 文件 | 删除原因 |
|------|---------|
| [SplashScreen.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/screens/splash/SplashScreen.kt) | 去掉启动页，直接进主页 |
| [IndustrialButton.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/components/IndustrialButton.kt) | 工业风组件全废弃 |
| [IndustrialCard.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/components/IndustrialCard.kt) | 同上 |
| [IndustrialInput.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/components/IndustrialInput.kt) | 同上（改用 Material TextField）|
| [IndustrialToggle.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/components/IndustrialToggle.kt) | 已经全局采用，替换默认的 Switch |
| [Color.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/theme/Color.kt)（旧） | 全部替换为新色板 |
| [Theme.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/theme/Theme.kt)（旧） | 全部替换为新主题 |
| [Type.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/ui/theme/Type.kt)（旧） | 替换为 Inter + JetBrains Mono |
| [ServerRepository.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/repository/ServerRepository.kt)（ui/screens/serverlist/） | 与 data/ 下重复，删除 |

### ➕ 全新创建

| 文件 | 功能 |
|------|------|
| `data/model/Group.kt` | 分组数据模型 |
| `data/dao/GroupDao.kt` | 分组数据库操作 |
| `data/repository/GroupRepository.kt` | 分组仓库 |
| `core/SubscriptionFetcher.kt` | HTTP 获取订阅 URL + 解析（最关键的新功能）|
| `ui/screens/subscription/SubscriptionScreen.kt` | 订阅管理页（全新）|
| `ui/screens/subscription/SubscriptionViewModel.kt` | 订阅 VM |
| `ui/screens/groups/GroupSheet.kt` | 分组管理弹层 |
| `ui/screens/groups/GroupViewModel.kt` | 分组 VM |
| `ui/screens/logs/LogsScreen.kt` | 日志查看页 |
| `ui/theme/Color.kt`（新） | 现代浅色/深色系统 (Soft Blue, Mint Green 等) |
| `ui/theme/Theme.kt`（新） | Modern Transparent Clean Warm 主题 |
| `ui/components/ConnectButton.kt` | 大圆形连接按钮 + 动画 |
| `ui/components/NodeCard.kt` | 节点列表卡片组件 |
| `ui/components/TrafficGraph.kt` | 流量折线图（从 HomeScreen 提取）|

---

## 第五章：关键技术缺口的修复方案

### 缺口 1：KCP + DTLS 传输支持（你的节点能否连上的关键）

**问题根源**：[CoreManager.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/CoreManager.kt) 中 [generateStreamSettings()](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/CoreManager.kt#319-359) 方法只有 TCP/WS/gRPC 三个分支，完全没有 KCP 的处理逻辑。当你的节点配置包含 `network: kcp` 时，App 会生成错误的 JSON 配置，Xray-core 无法解析，连接失败。

**修复内容**（修改 [CoreManager.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/core/CoreManager.kt) 约 20 行）：

```kotlin
"kcp" -> {
    put("kcpSettings", buildMap {
        put("mtu", 1350)          // 每个数据包的最大字节数
        put("tti", 50)            // 数据包间隔（毫秒）
        put("uplinkCapacity", 12) // 上行带宽估计（MB/s）
        put("downlinkCapacity", 100)
        put("congestion", false)
        put("readBufferSize", 2)
        put("writeBufferSize", 2)
        put("header", mapOf("type" to profile.kcpHeader))  // "dtls"
        if (profile.kcpSeed.isNotEmpty()) put("seed", profile.kcpSeed)
    })
}
```

同时在 [ServerProfile.kt](file:///d:/aidev/DOKODEMO/app/src/main/java/com/dokodemo/data/model/ServerProfile.kt) 新增两个字段：
```kotlin
val kcpHeader: String = "none",  // KCP 伪装类型：none/dtls/utp/srtp...
val kcpSeed: String = "",         // KCP 混淆密钥（可选）
```

### 缺口 2：libv2ray.aar 原生库（没有这个 App 就是空壳）

**说明**：Xray-core 是用 Go 语言写的，Android 不能直接运行 Go 代码。需要通过"gomobile"工具把它编译成 Android 能加载的 `.aar` 格式库文件。这个文件不能通过 Maven 直接下载，需要手动获取。

**获取方式**（一次性操作）：
1. 访问 `https://github.com/2dust/AndroidLibXrayLite/releases`
2. 下载最新的 `libv2ray.aar`
3. 放到项目 `app/libs/` 目录下
4. 同理下载 `geoip.dat` 和 `geosite.dat` 放到 `app/src/main/assets/`

### 缺口 3：订阅解析（全新功能）

需要新建 `SubscriptionFetcher.kt`，核心逻辑：

```
1. 用 OkHttp 发送 HTTP GET 请求到订阅 URL
2. 获取响应文本
3. 判断格式：
   - 如果第一行是 Base64 编码 → 解码后每行一个节点链接
   - 如果包含 "proxies:" → Clash YAML 格式，解析 YAML
4. 对每个链接调用 ShareLinkParser 解析成 ServerProfile
5. 存入 Room 数据库，绑定到对应订阅和分组
```

### 缺口 4：路由模式切换

**全局模式很重要**：DOKODEMO 当前硬编码了"绕过CN"路由规则（中国 IP 直连），这意味着连接代理后，中国 IP 的流量不走代理。但 TikTok 美国服务器 IP 是正常走代理的，所以实际影响不大。

但为了保险和简单，在"全局模式"下应该删除所有 CN 直连规则：

```kotlin
// 全局模式：删除这条规则
// mapOf("type" to "field", "domain" to listOf("geosite:cn"), "outboundTag" to "direct")

// 全局模式：删除这条规则  
// mapOf("type" to "field", "ip" to listOf("geoip:cn"), "outboundTag" to "direct")
```

---

## 第六章：开发阶段计划

| 周次 | 任务 | 文件 |
|------|------|------|
| **第1周** | 搭建新 UI 主题系统 | 删除 Industrial 组件，创建新 Color/Theme/组件 |
| **第1周** | 修复 KCP/DTLS + 安装 native libs | CoreManager.kt + ServerProfile.kt + libs 文件 |
| **第2周** | 重写 Home + NodeList + ConfigEditor UI | 三个主要 Screen 文件 |
| **第2周** | 重写 Settings UI + 路由模式切换 | SettingsScreen + CoreManager 路由逻辑 |
| **第3周** | 实现订阅功能全流程 | SubscriptionFetcher + SubscriptionScreen |
| **第3周** | 实现分组功能 | Group 实体 + GroupSheet + 节点移组 |
| **第3周** | 完善 ss:// 解析 + 日志页 | ShareLinkParser + LogsScreen |
| **第4周** | 联调测试：用你的 VMess 节点验证 | 端对端测试 |

---

## 第七章：验证测试方案

### 1. 用你的 VMess/KCP/DTLS 节点测试

在 App 的"添加节点"页面填写以下参数：

| 字段 | 值 |
|------|-----|
| 别名 | SanVPN |
| 服务器地址 | 104.238.140.227 |
| 端口 | 8080 |
| 协议 | VMess |
| UUID | 243ed98b-172a-4eea-be2b-ae9623xxxxxx |
| 额外 ID (alterId) | 0 |
| 加密 | auto |
| 传输协议 | KCP |
| KCP 伪装 | DTLS |

然后点击连接，期望结果：
- ✅ 通知栏出现"DokoDemo 已连接"
- ✅ 手机浏览器能访问 google.com
- ✅ TikTok App 能正常刷视频

### 2. TikTok 连通性验证

- 设置 → 路由模式 → 全局
- 连接节点
- 打开 TikTok → 能看到视频

### 3. 订阅导入验证

- 添加一个 v2rayN 格式订阅 URL
- 节点自动出现在列表中，延迟测试正常

### 4. DNS 泄漏检测（防止隐私泄露）

- 连接后访问 `https://dnsleaktest.com`
- 显示的服务器应该是代理服务器所在地，而非中国

---

> [!IMPORTANT]
> **下一步行动**：确认此规划后即开始编码。首要任务是安装 `libv2ray.aar`，然后修复 KCP 传输，这两步完成后你的 SanVPN 节点就能在 App 中正常连接了。
