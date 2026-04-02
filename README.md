# WordCrush

WordCrush 是一个基于 Android + Jetpack Compose 的英语单词学习应用。当前版本以本地词库学习为核心，包含每日固定词集、单词配对学习、限时模式、单词本检索、个人进度与本地/云端战绩同步。

## 项目现状

- UI 技术栈已经统一为 `Jetpack Compose`
- 项目结构采用 `Single Activity + Navigation Compose + ViewModel + Repository`
- 单词数据本地存储在 `Room`
- 用户会话、每日学习计划、活动游戏会话存储在 `DataStore`
- 网络接口通过 `Retrofit + OkHttp + Gson`
- 当前默认登录方式为本地管理员账号：
  - `admin`
  - `123456`

说明：
- 登录和 token 校验目前由客户端本地管理员逻辑接管
- 注册、改密、排行榜、游戏记录同步仍然依赖后端接口

## 主要功能

### 1. 每日固定词集

- 用户可在 `Profile` 页面设置每日学习单词数量
- 每天会从未掌握单词中随机抽取一批，生成当天固定词集
- 当天词集生成后保持不变，不会因为反复进入页面而重新洗牌
- 每个单词需要配对正确 `3` 次才会变为已掌握
- 如果中途配错，该单词会重新回到未掌握状态

### 2. Match / Timed 学习模式

- `Classic`：无时间限制
- `Timed`：倒计时模式
- 配对成功后卡片会消失
- 配对失败会显示错误反馈并扣除生命值
- 可在页面顶部查看最近一次正确配对的单词，并手动标记为“没记住”
- 游戏中途切页后会保持当前游戏状态
- 应用重开后会优先恢复未结束的游戏会话

### 3. Words 单词本

- 支持关键字搜索
- 支持 `All / Mastered / Learning` 筛选
- 支持播放英式/美式发音
- 支持单词状态标记：
  - `Mark`
  - `Reset`
- 单词列表采用按需加载与局部刷新策略

### 4. Profile

- 展示当前用户信息
- 展示 `Match / Timed` 分数汇总
- 设置每日学习数量
- 查看游戏记录
- 触发云端同步
- 修改密码
- 退出登录

## 技术栈

- Android SDK 36
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- ViewModel + StateFlow
- Room
- DataStore
- Retrofit + OkHttp
- Gson
- OpenCSV

## 目录结构

```text
app/src/main/java/com/example/wordcrush/
├── Activity/                 # MainActivity
├── data/
│   ├── api/                  # Retrofit API 定义
│   ├── local/                # DataStore / PreferenceManager
│   ├── model/                # 请求、响应、业务模型
│   └── repository/           # 数据访问与业务逻辑
├── Database/                 # Room 数据库、DAO、Entity、Converter
├── di/                       # Hilt 注入
├── ui/
│   ├── compose/              # Compose 页面与通用组件
│   ├── model/                # UI 模型
│   └── viewmodel/            # ViewModel
└── utils/                    # 应用状态、日志、工具类
```

## 关键文件

- [MainActivity.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/Activity/MainActivity.kt)
- [WordCrushApp.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/ui/compose/WordCrushApp.kt)
- [MainFlow.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/ui/compose/MainFlow.kt)
- [GameScreens.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/ui/compose/GameScreens.kt)
- [SupportScreens.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/ui/compose/SupportScreens.kt)
- [WordRepository.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/repository/WordRepository.kt)
- [AccountRepository.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/repository/AccountRepository.kt)
- [GameRecordRepository.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/repository/GameRecordRepository.kt)
- [ActiveGameSessionManager.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/repository/ActiveGameSessionManager.kt)

## 运行与构建

### 环境要求

- Android Studio
- JDK 17
- Android SDK 35/36

### 常用命令

```bash
./gradlew.bat :app:compileDebugKotlin
./gradlew.bat :app:assembleDebug
```

调试包默认输出位置：

- [app-debug.apk](E:/coding/word-crush-app/app/build/outputs/apk/debug/app-debug.apk)

## 后端接口

当前客户端实际使用到的接口文档见：

- [docs/backend-api.md](E:/coding/word-crush-app/docs/backend-api.md)

## 当前客户端与后端的边界

### 仍然依赖后端

- 用户注册
- 修改密码
- 排行榜读取
- 游戏记录上传
- 游戏记录删除
- 云端游戏记录全量同步

### 当前由客户端本地处理

- 登录
- token 校验
- 每日词集生成
- 单词学习进度
- 活动游戏会话恢复
- 单词发音播放逻辑

## 备注

- 当前默认服务地址在 [AppStateManager.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/utils/AppStateManager.kt) 中配置为 `http://192.168.201.21:8080`
- 如果后端地址变化，需要同步修改该文件或扩展为可配置项
