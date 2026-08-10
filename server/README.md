# WordCrush 后端服务

`server/` 是 WordCrush 的 Spring Boot 后端，向 Android 客户端和 Web 管理端提供用户、词库、学习进度、游戏记录和排行榜接口。服务端保存词库与学习数据的权威状态，客户端只负责缓存和离线变更队列。

## 技术栈

- Java 17
- Spring Boot 3.5
- Spring Web、Validation、Security
- Spring Data JPA
- MySQL 8.4
- Redis 7
- Flyway 数据库迁移
- JWT 会话认证
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper
- Docker / Docker Compose

## 模块结构

```text
server/src/main/java/com/wordcrush/server/
├── common/       # 统一响应、异常和公共组件
├── config/       # Spring、Web、OpenAPI 等配置
├── module/
│   ├── user/     # 用户账号、登录、注册、头像
│   ├── learning/ # 词库、学习状态、每日计划、同步
│   ├── game/     # 游戏记录和排行榜
│   └── admin/    # 管理端鉴权、用户管理、词表管理
└── security/     # JWT、Bearer Token 和当前用户上下文

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── db/migration/ # Flyway V1、V2、V3
├── static/       # 头像和其他静态资源
└── templates/    # resume.html 等服务端页面
```

## 数据职责

- MySQL：用户、词条、学习进度、每日计划、游戏记录和同步 mutation。
- Redis：运行时缓存和服务端短期状态。
- Flyway：按版本管理数据库结构；当前包含初始化、学习域和管理员角色迁移。
- `LearningWord` 是服务端词库实体，管理员停用词条时使用状态字段，不物理删除，以保留用户历史学习进度。
- 客户端通过 `/api/learning/sync` 提交离线学习变更，服务端按 `mutationId` 幂等处理。

## Profile

- `dev`：默认开发配置，适合本地运行和接口调试。
- `prod`：服务器部署配置，关闭 Swagger 与 Docker Compose 自动管理，只暴露必要的 Actuator 健康检查。

通过环境变量设置：

```text
SPRING_PROFILES_ACTIVE=dev|prod
```

## 本地启动

### Docker Compose（推荐）

在 `server/` 目录执行：

```powershell
docker compose --env-file .env.example `
  -p wordcrush-local up -d --build
```

`.env.example` 只用于本地开发，生产环境必须使用独立的 `.env`，不要把真实密码、JWT 密钥提交到 Git。

本地服务地址：

| 服务 | 地址 |
| --- | --- |
| Spring Boot API | `http://127.0.0.1:18080` |
| Actuator 健康检查 | `http://127.0.0.1:18080/actuator/health` |
| Web 管理端 | `http://127.0.0.1:18081` |
| MySQL | 仅 Compose 内部网络 |
| Redis | 仅 Compose 内部网络 |

查看状态和日志：

```powershell
docker compose --env-file .env.example -p wordcrush-local ps
docker compose --env-file .env.example -p wordcrush-local logs -f app
```

停止容器但保留数据卷：

```powershell
docker compose --env-file .env.example -p wordcrush-local down
```

### Maven 直接运行

如果 MySQL 和 Redis 已经可访问，也可以直接启动 Spring Boot：

```powershell
.\mvnw.cmd spring-boot:run
```

执行后端测试：

```powershell
.\mvnw.cmd -q test
```

## 环境变量

主要变量定义在 `.env.example`，生产环境由部署服务器注入：

| 变量 | 用途 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Spring profile，生产使用 `prod` |
| `SERVER_PORT` | Spring Boot 端口，Compose 中为 `8080` |
| `MYSQL_URL` | JDBC 连接地址 |
| `MYSQL_DATABASE` | 数据库名 |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 应用数据库账号 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接配置 |
| `JWT_SECRET` | JWT 签名密钥，必须使用随机长字符串 |
| `JWT_EXPIRATION_HOURS` | JWT 有效期 |
| `BOOTSTRAP_ADMIN_USERNAME` | 首次启动时初始化/提升的管理员账号 |
| `BOOTSTRAP_ADMIN_PASSWORD` | 管理员初始密码 |
| `WORDCRUSH_HOST_PORT` | 后端宿主机诊断端口，默认 `18080` |
| `ADMIN_HOST_PORT` | 管理端宿主机诊断端口，默认 `18081` |

## API 概览

所有 JSON 接口返回统一结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

受保护接口必须携带：

```text
Authorization: Bearer <token>
```

主要公共与用户接口：

- `POST /api/user/login`
- `POST /api/user/register`
- `GET /api/user/checkToken`
- `POST /api/user/changePassword`
- `POST /api/user/avatar`
- `GET /api/user/avatar/{username}`
- `POST /api/getTopNRecord`
- `POST /api/addGameRecord`
- `POST /api/deleteGameRecord`
- `POST /api/getAllGameRecord`

主要学习接口：

- `GET /api/learning/catalog`
- `GET /api/learning/state`
- `GET /api/learning/plan`
- `PUT /api/learning/settings/daily-target`
- `POST /api/learning/sync`

管理员接口全部要求已登录用户具有 `ADMIN` 角色：

- `GET /api/admin/me`
- `GET /api/admin/overview`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}/status`
- `PUT /api/admin/users/{id}/password`
- `GET /api/admin/words`
- `POST /api/admin/words`
- `PUT /api/admin/words/{id}`
- `POST /api/admin/words/import?replace=false|true`
- `GET /api/admin/words/export`

完整请求、响应和同步协议见 [docs/backend-api.md](../docs/backend-api.md)。

## 词表导入规则

管理端上传 UTF-8 CSV，字段顺序固定为：

```csv
id,english,pronunciation,chinese
1,abandon,/əˈbændən/,v. 遗弃；放弃
```

- `id` 必须为正整数且不能重复。
- `english` 最长 128 个字符。
- `pronunciation` 最长 255 个字符。
- `chinese` 最长 1024 个字符。
- 支持逗号、换行和双引号转义。
- 可以识别 `id`、`序号`、`编号` 等重复表头并跳过。
- `replace=false` 时只新增或更新 CSV 中的词条。
- `replace=true` 时，数据库中不在 CSV 内的现有词条会被停用，不会被删除。
- 导出接口只导出当前启用的词条，并带 UTF-8 BOM，便于 Excel 打开中文内容。

## Docker 部署

### 本地 Compose

`docker-compose.yml` 包含：

- `mysql`
- `redis`
- `app`
- `admin`

应用和管理端只绑定到宿主机回环地址；MySQL、Redis 不发布到宿主机端口。

### 生产服务器

生产环境使用 `docker-compose.server.yml` 加入现有 Relay 网络：

```powershell
docker compose --env-file .env `
  -p wordcrush-server `
  -f docker-compose.yml `
  -f docker-compose.server.yml `
  up -d --build
```

生产公网地址：

- API：`https://txy.hejulian.org/word-crush/`
- 管理端：`https://txy.hejulian.org/word-crush-admin/`

生产 app 的诊断端口仍为 `127.0.0.1:18080`，管理端为 `127.0.0.1:18081`。Caddy 通过 `wordcrush-app:8080` 和 `wordcrush-admin:80` 访问对应容器。

生产部署顺序建议为：

1. 本地执行测试、前端构建和 Compose 配置校验。
2. Git 提交并推送，服务器使用 `git pull --ff-only` 拉取。
3. 使用生产 Compose 文件构建并启动 app/admin。
4. 先校验 Caddyfile，再重载或仅重建 Caddy 容器。
5. 验证 `/actuator/health`、管理端页面和原 Relay 根路径。

## 相关文件

- [docker-compose.yml](./docker-compose.yml)：本地和基础 Compose 配置。
- [docker-compose.server.yml](./docker-compose.server.yml)：生产 Relay 网络覆盖配置。
- [.env.example](./.env.example)：本地环境变量示例。
- [Dockerfile](./Dockerfile)：Spring Boot 镜像构建文件。
- [src/main/resources/db/migration](./src/main/resources/db/migration)：数据库迁移。
- [../admin-web/README.md](../admin-web/README.md)：管理端说明。
