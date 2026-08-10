# WordCrush App

WordCrush 是一个“英语单词学习 + 配对闯关”系统，包含 Android 移动端、Spring Boot 后端和 TypeScript Web 管理端。移动端面向学习用户，管理端面向管理员维护用户和词库，后端负责统一账号、词库、学习进度、游戏记录与同步服务。

## 功能总览

### Android 移动端

- 注册、登录、会话恢复、修改密码和头像。
- 单词本浏览、搜索、发音和掌握状态。
- 经典配对、计时配对、游戏记录和排行榜。
- 每日学习计划、每日目标和学习进度。
- Room 缓存与离线变更队列，网络恢复后自动同步。

### Spring Boot 后端

- 用户账号和 JWT Bearer Token 鉴权。
- 服务端词库、学习状态、每日计划和学习同步。
- 游戏记录与排行榜。
- 管理员角色、用户管理和 CSV 词表管理。
- MySQL 持久化、Redis 缓存、Flyway 数据库迁移。

### Web 管理端

- 管理员登录和概览统计。
- 用户搜索、启停和密码重置。
- 词条搜索、编辑、新增、启停。
- CSV 词表上传、导出和按文件同步停用缺失词条。

## 仓库结构

```text
word-crush-app/
├── app/                    # Android 客户端（Kotlin + Jetpack Compose）
├── server/                 # Spring Boot 后端、数据库迁移、词库和 Docker Compose
├── admin-web/              # TypeScript + React 管理端
├── shared/api-contract/    # Android 与后端共享的 API 契约类
├── docs/                   # 接口契约等项目文档
├── gradle/                 # Gradle Version Catalog 和 Wrapper 配置
├── build.gradle.kts        # Android 根构建配置
└── settings.gradle.kts
```

各目录的具体说明：

- [移动端 README](./app/README.md)
- [后端 README](./server/README.md)
- [管理端 README](./admin-web/README.md)
- [后端 API 契约](./docs/backend-api.md)

## 系统架构

```text
Android App ───────┐
                   ├── HTTPS /word-crush/ ──► Spring Boot ──► MySQL
Admin Web ──────────┘                         │                Redis
                                             └── Flyway / JWT

Admin Web ── HTTPS /word-crush-admin/ ──► Caddy ──► Nginx ──► Spring Boot
```

服务端是词库与学习数据的最终来源。Android 端通过 Retrofit 访问 API，使用 Room 缓存词库和进度，并将离线操作写入 mutation queue；管理端通过 Nginx/Caddy 访问同一套管理员 API。词条停用采用状态字段，不物理删除，以保留用户历史学习进度。

## 技术栈

| 子系统 | 主要技术 |
| --- | --- |
| 移动端 | Kotlin、Jetpack Compose、Material 3、Hilt、Room、DataStore、Retrofit、OkHttp |
| 后端 | Java 17、Spring Boot 3.5、Spring Security、JPA、MySQL、Redis、Flyway、JWT |
| 管理端 | TypeScript、React 19、Vite 7、Nginx |
| 部署 | Docker Compose、Caddy、Relay 内部网络 |

## 地址与端口

生产环境：

```text
API       https://txy.hejulian.org/word-crush/
管理端    https://txy.hejulian.org/word-crush-admin/
```

本地 Docker Compose：

```text
后端      http://127.0.0.1:18080
管理端    http://127.0.0.1:18081
健康检查  http://127.0.0.1:18080/actuator/health
```

MySQL 和 Redis 默认只在 Compose 内部网络中提供服务，不发布到宿主机。

## 快速开始

### 启动后端和管理端

在仓库根目录执行：

```powershell
docker compose --env-file server/.env.example `
  -f server/docker-compose.yml `
  -p wordcrush-local up -d --build
```

然后访问 <http://127.0.0.1:18081>。本地管理员账号和密码来自 `server/.env.example`；生产部署必须替换示例密码、数据库密码和 JWT 密钥。

查看服务状态：

```powershell
docker compose --env-file server/.env.example `
  -f server/docker-compose.yml `
  -p wordcrush-local ps
```

### 构建 Android 客户端

环境要求：Android Studio、JDK 17、Android SDK Platform 36。执行：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Debug APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。

### 开发管理端

如果后端已运行在 `http://127.0.0.1:18080`：

```powershell
cd admin-web
npm ci
npm run dev
```

Vite 默认地址为 <http://127.0.0.1:5173>，会把 `/api` 代理到本地后端。生产子路径部署依赖相对资源路径和相对 API 路径，请参阅 [admin-web/README.md](./admin-web/README.md)。

## 验证命令

后端：

```powershell
cd server
.\mvnw.cmd -q test
docker compose --env-file .env.example -p wordcrush-local config --quiet
```

管理端：

```powershell
cd admin-web
npm run build
python smoke_test.py
```

移动端：

```powershell
cd ..
.\gradlew.bat :app:testDebugUnitTest
```

## 部署概览

生产部署使用 `server/docker-compose.yml` 和 `server/docker-compose.server.yml`：

```powershell
cd server
docker compose --env-file .env `
  -p wordcrush-server `
  -f docker-compose.yml `
  -f docker-compose.server.yml `
  up -d --build
```

部署后应检查：

1. `app`、`admin`、MySQL 和 Redis 容器状态。
2. `http://127.0.0.1:18080/actuator/health` 返回 `UP`。
3. 管理端页面和管理员 API 路由可通过 HTTPS 访问。
4. 原有 Relay 根路径仍然正常。

详细的环境变量、迁移、CSV 规则和服务器部署说明见 [server/README.md](./server/README.md)。

## 安全约定

- `.env`、真实数据库密码、JWT 密钥和管理员密码不得提交到 Git。
- 受保护 API 只使用 `Authorization: Bearer <token>`。
- 日志不记录密码、token、请求体或响应体。
- 管理端只允许 `ADMIN` 角色访问管理 API。
- 词条和用户默认使用停用状态代替物理删除，降低数据误操作风险。
