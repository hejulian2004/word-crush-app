# WordCrush Android 客户端

`app/` 是 WordCrush 的 Android 客户端，负责账号登录、单词学习、配对闯关、学习计划、排行榜和游戏记录。客户端使用 Jetpack Compose 构建界面，服务端负责词库、用户学习状态和跨设备同步。

## 功能范围

- 用户注册、登录、会话恢复、修改密码和头像上传。
- 单词本浏览、搜索、发音播放、掌握状态维护。
- 经典配对和计时配对两种闯关模式。
- 每日学习计划、每日目标、学习进度和掌握次数。
- 游戏记录、历史记录和排行榜。
- Room 本地缓存与离线学习变更队列，联网后批量同步到服务端。

## 技术栈

| 领域 | 技术 |
| --- | --- |
| 语言 | Kotlin 2.2.10，少量 Java Room 实体/DAO |
| UI | Jetpack Compose、Material 3、Navigation Compose |
| 架构 | ViewModel、UseCase、Repository、StateFlow、单向数据流 |
| 依赖注入 | Hilt |
| 本地数据 | Room、DataStore Preferences |
| 网络 | Retrofit、OkHttp、Gson |
| 图片与音频 | Glide、Youdao 发音接口 |
| 构建 | Gradle、Android Gradle Plugin 9.1 |

## Android 环境


- Android Studio，建议使用 Android Studio 自带的 JDK 17。
- Android SDK Platform 36。
- 最低 Android API 26，目标 API 36。
- Windows 构建需要确保 Android SDK、JDK 和 Gradle Wrapper 可用；不需要在项目中提交 `local.properties` 指向的本机 SDK 路径。

## 目录结构

```text
app/src/main/java/com/example/wordcrush/
├── Activity/       # MainActivity 和 Android 生命周期入口
├── Database/       # Room 数据库、单词/记录/离线变更 DAO
├── data/
│   ├── api/        # Retrofit 接口
│   ├── cache/      # 头像等缓存
│   ├── local/      # DataStore 和本地偏好
│   ├── model/      # 网络、本地和 UI 之间的数据模型
│   ├── network/    # URL、鉴权拦截器、统一错误处理
│   ├── remote/     # 远程数据源
│   └── repository/ # 业务数据入口
├── di/             # Hilt 网络、数据库和图片加载模块
├── domain/
│   ├── game/       # 配对游戏纯状态 Reducer
│   └── usecase/    # 账号、学习、游戏和记录用例
├── ui/
│   ├── architecture/# UDF Store、Action、State、Effect
│   ├── compose/    # Compose 页面、导航和主题
│   └── viewmodel/  # 页面状态与业务编排
└── utils/          # 日志、头像 URL 等工具
```

## 客户端架构

页面只负责展示 UI 和派发用户 Action，不直接访问网络或 Room：

```text
Compose UI
   │ Action
   ▼
ViewModel ──► UiState / UiEffect
   ▼
UseCase
   ▼
Repository
   ├── Room / DataStore
   └── RemoteDataSource
          ▼
       Retrofit / OkHttp
          ▼
        Spring Boot
```

一次性导航、Snackbar 和音频播放通过 Effect 传递，不放进 `StateFlow`，避免 Compose 重组时重复执行。经典和计时配对共用 `MatchGameReducer` 处理核心规则，计时器、记录保存和活动会话持久化由 UseCase/Repository 负责。

## 数据同步与离线行为

服务端是词库和学习进度的最终来源，客户端的 Room 主要用于缓存和离线体验。

1. 登录后，客户端分页拉取服务端词库，并恢复用户学习状态和每日计划。
2. 用户在离线状态下完成学习、标记掌握或修改每日目标时，变更写入本地 mutation queue。
3. 网络恢复后，客户端以批次调用 `POST /api/learning/sync`，服务端按 `mutationId` 幂等处理。
4. 首次迁移已有本地学习数据时，客户端使用 `IMPORT_SNAPSHOT` 提交掌握次数；服务端保留更高的掌握次数。
5. 应用结束时会持久化仍在进行中的游戏会话，避免异常退出导致状态丢失。

## 网络配置

生产 API 地址当前为：

```text
https://txy.hejulian.org/word-crush/
```

配置文件：`src/main/java/com/example/wordcrush/data/network/NetworkConfig.kt`。

发音服务地址为 `https://dict.youdao.com/`。Retrofit 请求路径必须保持相对路径，例如 `api/user/login`，不要在 `ApiPaths.kt` 中添加开头的 `/`，否则可能绕过 `/word-crush/` 前缀。

网络客户端按职责分为：

- 公共 API：登录、注册、排行榜和头像读取。
- 鉴权 API：会话校验、修改密码、头像上传和游戏记录。
- 学习 API：词库、学习状态、每日计划、每日目标和离线同步。

鉴权请求使用：

```text
Authorization: Bearer <token>
```

`SessionManager` 是 token、用户名、uid 和头像状态的唯一会话来源。日志只允许记录脱敏后的方法、URL、状态码和耗时，不得记录密码、token、请求体或响应体。

详细接口契约见 [docs/backend-api.md](../docs/backend-api.md)。

## 构建与测试

在仓库根目录执行：

```powershell
# 编译 Kotlin
.\gradlew.bat :app:compileDebugKotlin

# 单元测试
.\gradlew.bat :app:testDebugUnitTest

# 构建 Debug APK
.\gradlew.bat :app:assembleDebug
```

生成的 Debug APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

也可以直接用 Android Studio 打开仓库根目录，选择 `app` 配置运行到 API 26 及以上的模拟器或真机。

## 联调流程

先启动后端和管理端容器：

```powershell
docker compose --env-file server/.env.example `
  -f server/docker-compose.yml `
  -p wordcrush-local up -d --build
```

本地后端诊断地址是 `http://127.0.0.1:18080`。如果 Android 模拟器需要访问宿主机服务，应使用模拟器对应的宿主机地址（通常为 `10.0.2.2`），并同步调整 `NetworkConfig.API_BASE_URL`；真机调试时使用开发机局域网 IP。生产构建继续使用代码中的 HTTPS 地址。

## 常见问题

- Gradle 构建失败：确认 Android Studio 使用 JDK 17，并已安装 SDK Platform 36。
- 登录成功但词库为空：确认服务端健康检查正常，并检查 `GET /api/learning/catalog` 是否返回数据。
- 401 后反复回到登录页：检查 token 是否过期，以及请求是否使用 `Authorization: Bearer`。
- 离线数据没有同步：恢复网络后重新进入学习页或触发学习操作，客户端会重试 pending mutations；服务端同步按 `mutationId` 保证幂等。
