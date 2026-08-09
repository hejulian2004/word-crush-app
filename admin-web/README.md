# Word Crush Admin Studio

TypeScript + React 管理端，和 `server/` 里的 Spring Boot API 一起通过 Docker Compose 运行。

## 本地开发

在仓库根目录启动后端依赖与管理端：

```powershell
docker compose --env-file server/.env.example -f server/docker-compose.yml up --build -d
```

然后打开 <http://127.0.0.1:18081>。默认管理员账号由 `server/.env.example` 中的 `BOOTSTRAP_ADMIN_USERNAME` 与 `BOOTSTRAP_ADMIN_PASSWORD` 初始化；生产环境请替换密码和 JWT 密钥。

如果只想用 Vite 热更新：

```powershell
cd admin-web
npm install
npm run dev
```

Vite 会把 `/api` 代理到 `http://127.0.0.1:18080`。

## CSV 格式

```csv
id,english,pronunciation,chinese
1,abandon,/əˈbændən/,v. 遗弃；离开；放弃
```

上传时支持“同步停用缺失词条”。关闭时只新增/更新文件中的 ID；开启时，数据库中不在文件里的词条会变成停用状态，不会物理删除，因此不会破坏已有学习进度。
