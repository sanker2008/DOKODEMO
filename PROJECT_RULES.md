# DokoDemo 项目规则与技能指南

本文档定义了 DokoDemo Android 客户端的核心开发规则、设计规范以及常见开发任务的操作技能（Skills）。所有贡献者（包括 AI 助手）应严格遵守此文档。

---

## 1. 核心规则 (Core Rules)

### 1.1 UI 设计规范：Neubrutalism (新野兽派)
DokoDemo 采用极其严格的 **Industrial Neubrutalism** 风格。

*   **原则**: Function over Form (功能至上)，拒绝装饰性元素。
*   **色彩系统**:
    *   **背景**: 必须使用 `IndustrialBlack` (`#000000`)。
        *   ❌ 禁止使用深灰、海军蓝或其他深色作为背景。
    *   **强调色**: `AcidLime` (`#CCFF00`)。
        *   ✅ 用于：光标、激活状态、重要数据强调、进度条。
    *   **文本**:
        *   标题/静态文本: `IndustrialWhite` (`#FFFFFF`) 或 `TextGrey` (`#888888`)。
        *   动态数据: `MonospaceFont` (JetBrains Mono)。
*   **组件样式**:
    *   **圆角 (Corner Radius)**: **0dp**。
        *   ✅ 所有按钮、卡片、输入框必须是直角矩形。
        *   ⚠️ 唯一例外：极少数标签（Tag）可使用小圆角，但首选直角。
    *   **边框 (Borders)**: 必须可见。
        *   ✅ 使用 `1.dp` 或 `2.dp` 的 `IndustrialGrey` 或 `AcidLime` 边框。
    *   **阴影 (Elevation)**: **0.dp**。
        *   ❌ 严禁使用任何形式的阴影。

### 1.2 架构规范
项目遵循 Google 推荐的现代 Android 架构。

*   **技术栈**: Kotlin, Jetpack Compose, Hilt, Room, Coroutines/Flow。
*   **分层**:
    *   `ui/`: Composable Screens, ViewModels.
    *   `data/`: Repositories, Room DAOs, Data Sources.
    *   `core/`: V2Ray/Xray 核心逻辑 (CoreManager)。
    *   `service/`: Android Services (VpnService)。
*   **V2Ray 集成规则**:
    *   ❌ UI 层（Screen/ViewModel）**禁止**直接调用 `Libv2ray` 或 `Tun2socks` 原生方法。
    *   ✅ 所有 V2Ray 操作必须通过 `CoreManager` 进行。
    *   ✅ 所有 VPN 操作必须通过 `VpnController` 进行。

---

## 2. 技能指南 (Development Skills)

以下定义了开发常见功能的标准工作流。

### Skill: 添加新屏幕 (Adding a New Screen)

当你需要添加一个新的功能页面时，请遵循以下流程：

1.  **定义路由**:
    *   在 `app/src/main/java/com/dokodemo/navigation/NavRoutes.kt` 中添加新的 `Route` 对象。
2.  **创建 ViewModel**:
    *   创建 `HiltViewModel`，定义 `UiState` 数据类（使用 `data class`）。
    *   使用 `StateFlow` 暴露 UI 状态。
3.  **创建 Screen Composable**:
    *   使用 `Scaffold`，并设置 `containerColor = IndustrialBlack`。
    *   使用 `viewModel: MyViewModel = hiltViewModel()` 获取实例。
    *   **必须**遵循 UI 设计规范（纯黑背景、无圆角组件）。
4.  **注册导航**:
    *   在 `app/src/main/java/com/dokodemo/navigation/DokoNavHost.kt` 中添加 `composable(Route.NewScreen.path) { ... }`。

### Skill: 创建工业风组件 (Creating Industrial Components)

创建自定义 UI 组件时：

1.  **容器**: 使用 `IndustrialCard` 或 `Box` 带 `border`。
2.  **状态反馈**:
    *   不要使用 Ripple Effect (水波纹)。
    *   使用**颜色反转** (Color Inversion) 或 **边框加粗/变色** 来表示点击/激活状态。
3.  **字体使用**:
    *   静态标签 -> `FontWeight.Bold` Sans-serif。
    *   数值/IP/代码 -> `MonospaceFont`。

### Skill: 调试 V2Ray 核心 (Debugging Core)

当 VPN 连接遇到问题时：

1.  **检查本地库**:
    *   确认 `app/libs/libv2ray.aar` 是否存在且大小正确 (>10MB)。
2.  **查看日志**:
    *   使用 Logcat 过滤 Tag: `DokoDemoVpnService` 或 `CoreManager`。
    *   `adb logcat -s "DokoDemoVpnService" "CoreManager" "GoLog"`
3.  **配置验证**:
    *   `CoreManager` 会在启动前生成 JSON 配置。
    *   可以在 Logcat 中查看生成的 JSON (搜索 "V2Ray Config")。

### Skill: 提交代码 (Committing Code)

*   **Kotlin 风格**: 遵循官方 Kotlin Coding Conventions。
*   **无用代码**: 提交前删除所有未使用的 imports 和注释掉的代码块。
*   **字符串**: 尽量通过 `strings.xml` 管理用户可见的文本（除了原型阶段的硬编码）。

---

**DOKODEMO** // CODING_RULES // V.1.0
