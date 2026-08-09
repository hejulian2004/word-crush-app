# WordCrush App

WordCrush 是一个基于 Android + Jetpack Compose 的英语单词学习与配对闯关应用，包含单词本、闯关模式、计时模式、排行榜、游戏记录、头像上传和每日学习计划。

## 技术栈

- Kotlin / Jetpack Compose / Material 3
- Hilt / ViewModel / StateFlow / 单向数据流
- Room / DataStore
- Retrofit + OkHttp + Gson
- Glide

## 客户端分层与单向数据流

```text
Compose UI
  │ Action
  ▼
ViewModel ───────────────► UiEffect ──► 导航 / Snackbar / 音频
  │
  ├─ UiState ◄── Reducer（纯状态转换）
  ▼
UseCase
  ▼
Repository
 ├─ LocalDataSource       // Room、DataStore
 └─ RemoteDataSource
     ├─ Public HTTP API
     ├─ Authenticated HTTP API
     └─ SocketDataSource
          ↓
   Retrofit / OkHttp
          ↓
   Server
```

每个页面通过 `Action` 向 ViewModel 发送用户意图，ViewModel 通过 UseCase
执行业务操作并更新不可变 `UiState`。一次性行为使用 `Effect`，不放入
`StateFlow`，因此重组不会重复触发导航、提示或音频。

经典和计时配对共用 `MatchViewModel` 与纯 `MatchGameReducer`；计时、进度、
记录保存和活动会话持久化由 UseCase 负责。

Repository 不直接依赖 Retrofit 或 OkHttp。网络层通过 Hilt 区分公共 HTTP、鉴权 HTTP、公共 WebSocket 和鉴权 WebSocket 客户端。

## 网络客户端

- 公共 HTTP：登录、注册、排行榜、头像读取和第三方发音。
- 鉴权 HTTP：checkToken、修改密码、头像上传和游戏记录操作。
- WebSocket：提供公共/鉴权长连接基础设施，当前尚未接入具体实时业务。
- 鉴权请求只发送 `Authorization: Bearer <token>`。
- 鉴权 HTTP/Socket 收到 401 后清理会话；公共请求不会触发退登。
- Debug 日志只记录脱敏的请求方法、URL、状态码和耗时，不记录密码、token、请求体或响应体。

默认 API 地址：

```text
https://txy.hejulian.org/word-crush/
```

配置位置：`app/src/main/java/com/example/wordcrush/data/network/NetworkConfig.kt`。

## 会话行为

`SessionManager` 是 token、用户名、uid 和头像状态的唯一来源，DataStore 只负责持久化。

- 登录成功后保存服务端会话。
- 冷启动恢复会话并调用鉴权 `checkToken`。
- token 失效、其他设备登录或改密后，下一次鉴权请求会回到登录页。
- 退出登录只清理会话数据，不影响应用公共配置。

## 主要接口

- `POST /api/user/login`
- `POST /api/user/register`
- `GET /api/user/checkToken`（Bearer 鉴权，无 query token）
- `POST /api/user/changePassword`（Bearer 鉴权）
- `POST /api/user/avatar`（Bearer 鉴权）
- `GET /api/user/avatar/{username}`（公共资源）
- `POST /api/getTopNRecord`（公共接口）
- `POST /api/addGameRecord`（Bearer 鉴权）
- `POST /api/deleteGameRecord`（Bearer 鉴权）
- `POST /api/getAllGameRecord`（Bearer 鉴权）

所有 JSON API 使用统一响应格式：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

详细字段见 [docs/backend-api.md](./docs/backend-api.md)。

## 编译

环境要求：Android Studio、JDK 17/兼容的 Android Studio JBR、Android SDK 35/36。

```bash
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:assembleDebug
```

调试 APK：`app/build/outputs/apk/debug/app-debug.apk`。
