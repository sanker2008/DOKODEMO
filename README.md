# DokoDemo

DokoDemo 是一款基于 Android 的轻量级、高性能 V2Ray/Xray 客户端。

## 1. 核心技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose (Material Design 3)
- **架构**: MVVM (Model-View-ViewModel)
- **依赖注入**: Hilt
- **持久化**: Room (节点/订阅管理), DataStore (应用设置)
- **异步与响应式**: Coroutines & Flow
- **代理核心**: Xray-core (通过 JNI 桥接 `libv2ray.aar`)
- **VPN 接口**: Android `VpnService` + `tun2socks` (实现全局与分应用代理)

## 2. 核心原理解析

DokoDemo 使用 **VPN 技术做流量劫持 + 代理协议做数据传输** 的组合方案。

1. **流量劫持**: `DokoDemoVpnService` 创建虚拟网卡 (TUN 设备)，拦截系统流量。
2. **协议转换**: `tun2socks` 读取虚拟网卡数据，将 TCP/UDP 包转换为 SOCKS5 协议流量。
3. **加密与伪装**: `Xray-core` 在本地监听 SOCKS5 端口，使用节点配置 (VMess, VLESS, Shadowsocks 等) 加密流量。
4. **代理传输**: 发送给远端代理服务器，实现科学上网。

## 3. 视觉与设计规范 (Design System)

项目摒弃了初期的工业风 (Neubrutalism)，转而采用 **现代、通透、干净且温暖 (Modern, Transparent, Clean, Warm)** 的设计风格。

- **核心配色**:
  - 基础背景: `#F0F4F7` (Light) / `#121212` (Dark)
  - 主操作色 (Primary): `#A0C4E3` (Soft Blue)
  - 文本/图标: `#607D8B` (Slate)
  - 连接/成功状态 (Accent): Soft Mint Green `#B7D5C7` (用于呼吸灯及连接状态)
- **UI 元素**: 采用大圆角 (最小 16dp) 和轻微的玻璃拟物化 (Glassmorphism) 效果。取消了生硬的边框和强烈的对比度。
- **状态反馈**: 连接成功后，主页显示平缓的呼吸光环动画。

## 4. 核心功能模块

1. **主页 (Dashboard)**: 
   - 大尺寸圆形连接按钮，配合连接后的呼吸灯动效。
   - 实时流量监控 (上传/下载)。
   - 快速切换当前节点。
2. **节点管理 (Server List)**:
   - 节点列表展示与分组筛选。
   - 扫码添加、剪贴板解析、手动配置编辑。
   - 延迟测试 (Ping)。
3. **分应用代理 (Split Tunneling)**:
   - 允许用户勾选特定 App 走代理或直连，配合工业风开关组件 `IndustrialToggle`。
4. **应用设置 (Settings)**:
   - 路由模式选择 (全局、绕过国内、仅代理选中)。
   - 外观与字体大小调节。
   - 语言切换 (简体中文、繁體中文、English)。
   - 高级网络设置 (Mux, UDP 代理, 广告过滤等)。

## 5. 原生库依赖 (Native Libraries Setup)

DokoDemo 的核心代理能力依赖于以下原生库，开发前请确保环境正确：

1. **libv2ray.aar (Xray-core 核心)**:
   - 放置路径: `app/libs/libv2ray.aar`
2. **GeoIP / GeoSite 数据**:
   - 放置路径: `app/src/main/assets/geoip.dat` 和 `app/src/main/assets/geosite.dat`
   - 用于路由规则的域名和 IP 匹配。

## 6. 开发规范

- **状态管理**: ViewModel 中统一使用 `data class UiState` 并在 `StateFlow` 中暴露。
- **UI 绘制**: 遵循最新的配色和排版系统 (`DokoDemoTheme`)。使用 `AppPreferences` 实现字体大小缩放和深色模式跟随。
- **多语言**: 必须使用 `strings.xml` 进行文本硬编码的抽离，以便于多语言支持。
