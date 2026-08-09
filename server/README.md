# WordCrush Server

WordCrush Server is the backend for the WordCrush app. It uses Spring Boot 3, MySQL, Redis, Flyway, JWT, and Docker.

## Stack

- Java 17
- Spring Boot 3
- Spring Web / Validation / Security
- Spring Data JPA
- MySQL 8
- Redis 7
- Flyway
- JWT
- Springdoc OpenAPI / Swagger UI
- Docker / Docker Compose
- Maven

学习域以服务端词库和进度为准，Android 客户端只保留 Room 缓存及离线变更队列。开发阶段允许直接调整 schema 和接口，不维护旧接口兼容层。

## Profiles

This project uses explicit Spring profiles:

- `dev` is the default profile
- `prod` is used for server deployment

The active profile is controlled by `SPRING_PROFILES_ACTIVE`.

## Local Development

Run the app with the default `dev` profile:

```bash
./mvnw spring-boot:run
```

In `dev`, Swagger and Actuator details stay enabled, and Spring Boot Docker Compose integration is on.

## Production Deployment

Run the Docker stack with the production profile:

```bash
docker compose up --build -d
```

The compose file sets `SPRING_PROFILES_ACTIVE=prod`, so the app starts with production settings automatically.

Production behavior:

- Swagger is disabled
- Spring Boot Docker Compose integration is disabled
- Actuator only exposes `health`
- JVM memory is capped
- MySQL and Redis container memory are capped
- `resume.html` is served from a bind-mounted directory, so you can edit it on the host without rebuilding the image
- `/images/avatar.jpg` is also served from a bind-mounted directory for the same reason

WordCrush deployment uses the existing relay Docker network and HTTPS entrypoint:

- backend container port: `8080`
- host diagnostic binding: `127.0.0.1:18080`
- admin web container port: `80`
- admin host diagnostic binding: `127.0.0.1:18081`
- public API prefix: `https://txy.hejulian.org/word-crush/`
- public admin prefix: `https://txy.hejulian.org/word-crush-admin/`
- MySQL and Redis are internal Compose services and are not published to the host

For local Docker testing, pass `.env.example` explicitly:

```bash
docker compose --env-file .env.example up --build -d
```

For the Tencent Cloud deployment, use the server overlay so the app joins the existing relay network:

```bash
docker compose --env-file .env -f docker-compose.yml -f docker-compose.server.yml up --build -d
```

Mounted files used by the app:

- `src/main/resources/templates/resume.html` -> `/app/external-resume/resume.html`
- `src/main/resources/static/images/avatar.jpg` -> `/app/external-static/images/avatar.jpg`
- `/` redirects to `/resume.html`

## Key Environment Variables

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_DATABASE`
- `JWT_SECRET`
- `JWT_EXPIRATION_HOURS`
- `BOOTSTRAP_ADMIN_USERNAME`
- `BOOTSTRAP_ADMIN_PASSWORD`

## API Notes

- `POST /api/user/login`
- `POST /api/user/register`
- `GET /api/user/checkToken` (Bearer authentication required)
- `POST /api/getTopNRecord`
- `GET /api/user/avatar/{username}`
- `GET /api/learning/catalog`
- `GET /api/learning/state`
- `GET /api/learning/plan`
- `PUT /api/learning/settings/daily-target`
- `POST /api/learning/sync`
- `GET /api/admin/me`
- `GET /api/admin/overview`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}/status`
- `PUT /api/admin/users/{id}/password`
- `GET /api/admin/words`
- `POST /api/admin/words`
- `PUT /api/admin/words/{id}`
- `POST /api/admin/words/import` (multipart CSV)
- `GET /api/admin/words/export`
- `/swagger-ui/**`
- `/api-docs/**`
- `/actuator/**`

JSON APIs use the standard `{code, msg, data}` response envelope. `code` is the
HTTP status code and only `200` represents success. Successful responses use
`msg: "success"`; responses without business data use `data: null`. Error
responses keep the same envelope, use the matching HTTP status/code, and never
expose server internals. Protected requests must send
`Authorization: Bearer <token>`; the legacy `token` header and query-token form
are no longer accepted.

## Useful Links

- [docs/database-design.md](./docs/database-design.md)
- [src/main/resources/db/migration/V1__init_schema.sql](./src/main/resources/db/migration/V1__init_schema.sql)
- [src/main/resources/db/migration/V2__learning_schema.sql](./src/main/resources/db/migration/V2__learning_schema.sql)
