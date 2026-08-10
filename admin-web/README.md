# WordCrush Admin Studio

`admin-web/` 是 WordCrush 的 Web 管理端，面向管理员提供用户运营和词库维护能力。前端使用 TypeScript + React，生产镜像使用 Nginx，后端接口由 `server/` 提供。

## 功能

- 管理员登录和管理员身份校验。
- 概览统计：用户总数、启用用户、词条总数、启用词条。
- 用户搜索、状态启停和密码重置。
- 词条搜索、分页、编辑、新增和启停。
- CSV 词表导入、导出和“同步停用缺失词条”。
- 响应式布局，支持桌面端和较窄窗口。

用户或词条的“删除”实际使用停用状态，避免破坏已有学习进度；系统不允许停用当前登录管理员，也不允许停用最后一个启用中的管理员。

## 技术栈

- TypeScript
- React 19
- Vite 7
- Nginx Alpine
- 原生 `fetch` API、React 状态管理和 CSS，不依赖额外 UI 组件库

## 目录结构

```text
admin-web/
├── src/
│   ├── App.tsx       # 登录、概览、用户和词库页面
│   ├── api.ts        # API 请求、token 和文件下载
│   ├── types.ts      # 管理端数据类型
│   ├── main.tsx      # React 入口
│   └── styles.css    # 全局视觉样式和响应式布局
├── Dockerfile        # Node 构建 + Nginx 运行时镜像
├── nginx.conf        # SPA fallback 和 /api 反向代理
├── smoke_test.py     # Playwright 浏览器冒烟测试
├── package.json
└── vite.config.ts
```

## 本地开发

### Docker 方式

从仓库根目录启动后端、数据库、Redis 和管理端：

```powershell
docker compose --env-file server/.env.example `
  -f server/docker-compose.yml `
  -p wordcrush-local up -d --build
```

访问：

```text
http://127.0.0.1:18081
```

本地管理员账号由 `server/.env.example` 中的 `BOOTSTRAP_ADMIN_USERNAME` 和 `BOOTSTRAP_ADMIN_PASSWORD` 初始化。生产环境必须替换密码和 JWT 密钥。

### Vite 热更新

后端容器或后端服务需要先运行在 `http://127.0.0.1:18080`，然后执行：

```powershell
cd admin-web
npm ci
npm run dev
```

Vite 默认监听 `http://127.0.0.1:5173`，并把 `/api` 代理到 `http://127.0.0.1:18080`。也可以通过 `VITE_API_BASE_URL` 指定其他 API 前缀：

```powershell
$env:VITE_API_BASE_URL = 'http://127.0.0.1:18080/api'
npm run dev
```

默认 API 地址是 `./api`，使用相对路径是为了让应用部署在 `/word-crush-admin/` 子路径时，浏览器仍然请求到正确的管理端 API。

## 构建与检查

```powershell
npm ci
npm run build
```

生产构建产物输出到 `dist/`，Dockerfile 会把它复制到 Nginx 的 `/usr/share/nginx/html`。

如果已经安装 Python Playwright 和 Chromium，可以运行本地浏览器冒烟测试：

```powershell
python smoke_test.py
```

测试会登录本地管理端，并检查概览、用户列表、词库列表以及浏览器控制台错误。默认地址是 `http://127.0.0.1:18081`，默认账号读取 `WORDCRUSH_ADMIN_USERNAME` 和 `WORDCRUSH_ADMIN_PASSWORD` 环境变量；未设置时使用 `.env.example` 的本地示例账号。

## CSV 词表格式

导入文件使用 UTF-8 CSV，字段顺序固定为：

```csv
id,english,pronunciation,chinese
1,abandon,/əˈbændən/,v. 遗弃；离开；放弃
2,"break down","/breɪk daʊn/","v. 出故障；分解"
```

导入规则：

- `id` 为正整数，文件内不能重复。
- `english` 最长 128 个字符。
- `pronunciation` 最长 255 个字符。
- `chinese` 最长 1024 个字符。
- 支持字段中的逗号、换行和双引号转义。
- `id`、`序号`、`编号` 等表头行会自动跳过。
- 普通导入只新增或更新文件中的词条。
- 勾选“同步停用缺失词条”后，数据库里不在文件中的启用词条会被停用，不会物理删除。
- 导出只包含启用词条，文件名为 `wordbook.csv`。

## 管理端 API

管理端登录后把 JWT 保存到浏览器 `localStorage`，请求通过 `Authorization: Bearer <token>` 发送：

```text
POST /api/user/login
GET  /api/admin/me
GET  /api/admin/overview
GET  /api/admin/users
PUT  /api/admin/users/{id}/status
PUT  /api/admin/users/{id}/password
GET  /api/admin/words
POST /api/admin/words
PUT  /api/admin/words/{id}
POST /api/admin/words/import?replace=false|true
GET  /api/admin/words/export
```

后端接口的统一响应结构和字段说明见 [../docs/backend-api.md](../docs/backend-api.md)。

## Docker 与生产访问

`Dockerfile` 使用两阶段构建：

1. `node:22-alpine` 执行 `npm ci` 和 `npm run build`。
2. `nginx:1.27-alpine` 提供静态文件，并把 `/api/` 代理到 Docker 网络中的 `app:8080`。

服务器使用 Compose overlay 加入 Relay 内部网络，公网访问地址为：

```text
https://txy.hejulian.org/word-crush-admin/
```

由于部署在子路径下，Caddy 会把 `/word-crush-admin/` 前缀去掉后转发给 Nginx；Vite 的 `base: './'` 和 API 的相对路径配置不能随意改成根路径。

生产部署命令见 [../server/README.md](../server/README.md)。
