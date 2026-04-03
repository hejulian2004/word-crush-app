# WordCrush App

WordCrush 是一个基于 Android + Jetpack Compose 的英语单词学习与配对闯关应用，包含单词本、闯关模式、计时模式、排行榜、游戏记录、头像上传、每日学习计划等能力。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- ViewModel + StateFlow
- Room
- DataStore
- Retrofit + OkHttp + Gson

## 当前登录与会话行为

客户端现在已切换为服务端会话模式，不再依赖本地伪登录逻辑。

- 登录成功后，保存服务端返回的 `token / username / uid`
- 所有 Retrofit 请求都会自动附带 token
- 自动携带两种请求头，兼容当前后端：
  - `Authorization: Bearer <token>`
  - `token: <token>`
- 冷启动时会先调用 `checkToken` 校验本地 token
- 如果 token 已失效，应用会清理本地会话并回到登录页
- 如果运行过程中任意接口返回 `401`，也会立即自动退出登录并跳回登录页

这意味着当服务端发生以下情况时，客户端会被踢回登录页：

- token 过期
- 同账号在其他设备重新登录
- 用户改密导致旧 token 全部失效

## 主要功能

### 1. 登录与账号

- 用户登录
- 用户注册
- token 校验
- 修改密码
- 上传头像
- 退出登录

### 2. Match / Timed 游戏

- `Match` 模式
- `Timed` 模式
- 本地成绩保存
- 成绩云端同步
- 游戏记录查询与删除
- 排行榜展示

### 3. 单词本

- 单词搜索
- `All / Mastered / Learning` 过滤
- 英式 / 美式发音播放
- `Mark / Reset` 掌握状态切换
- Room 本地持久化

### 4. Profile

- 展示用户名与头像
- 展示 Match / Timed 最高分
- 每日学习计划
- 云端数据同步
- 修改密码
- 退出登录

## 目录结构

```text
app/src/main/java/com/example/wordcrush
├── Activity
├── data
│   ├── api
│   ├── cache
│   ├── local
│   ├── model
│   └── repository
├── Database
├── di
├── ui
│   ├── compose
│   ├── model
│   └── viewmodel
└── utils
```

## 关键实现位置

- `app/src/main/java/com/example/wordcrush/di/NetworkModule.kt`
  - Retrofit / OkHttp 配置
  - 统一注入 token
  - 全局 `401` 自动退登
- `app/src/main/java/com/example/wordcrush/data/repository/AccountRepository.kt`
  - 登录、校验 token、注册、改密、头像上传、退出登录
- `app/src/main/java/com/example/wordcrush/data/local/PreferenceManager.kt`
  - DataStore 会话与用户信息存储
- `app/src/main/java/com/example/wordcrush/utils/AppStateManager.kt`
  - 内存态会话、头像、全局会话失效事件
- `app/src/main/java/com/example/wordcrush/ui/viewmodel/MainViewModel.kt`
  - 启动时 token 校验
  - 会话失效后跳转登录页

## 网络联调约定

当前客户端默认依赖以下服务端能力：

- `POST /api/user/login`
- `GET /api/user/checkToken`
- `POST /api/user/register`
- `POST /api/user/changePassword`
- `POST /api/user/avatar`
- `GET /api/user/avatar/{username}`
- `POST /api/getTopNRecord`
- `POST /api/addGameRecord`
- `POST /api/deleteGameRecord`
- `POST /api/getAllGameRecord`

更详细的联调说明见：

- [docs/backend-api.md](./docs/backend-api.md)

## 编译与运行

环境要求：

- Android Studio
- JDK 17
- Android SDK 35/36

常用命令：

```bash
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:assembleDebug
```

调试 APK：

- `app/build/outputs/apk/debug/app-debug.apk`

## 服务端地址

默认后端地址配置在：

- `app/src/main/java/com/example/wordcrush/utils/AppStateManager.kt`

当前默认值：

- `http://192.168.201.21:8080`

如果后端地址变化，需要同步修改这里。

## 数据存储

- 用户会话、头像地址、每日学习计划、活动游戏会话：`DataStore`
- 单词、学习状态等本地结构化数据：`Room`

## 会话相关说明

- 登录成功后，客户端会把 token 写入 `DataStore`
- 启动时会先校验 token，再决定进入主流程还是登录页
- 全局网络层会在已登录请求收到 `401` 时自动清理会话
- 由于服务端启用了单设备登录，账号在新设备登录后，旧设备会在下一次请求时自动退登
