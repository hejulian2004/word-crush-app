# WordCrush 后端接口文档

本文档描述当前客户端和 sibling Spring Boot 服务端共同使用的接口契约。

## 基本信息

- Base URL：`https://txy.hejulian.org/word-crush/`
- HTTP 客户端：Retrofit + OkHttp + Gson
- 鉴权方式：`Authorization: Bearer <token>`
- 不再支持 `token` 请求头或 query token
- 当前模块化重构保持既有接口路径、字段和统一响应结构；本次不新增 schema 迁移，
  仅调整服务端内部模块边界和持久化映射。

## 统一响应

所有 JSON API 返回：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

- `code` 与 HTTP 状态码保持一致，只有 `code == 200` 表示成功。
- 所有成功 JSON 接口的 `msg` 固定为 `success`。
- 无业务数据的成功响应使用 `data: null`。
- 失败请求使用对应 HTTP 状态码，并返回相同结构，错误响应的 `data` 固定为 `null`。
- 4xx 错误返回具体英文提示，5xx 错误统一返回 `internal server error`。

常用错误码：

| HTTP/code | 含义 |
| --- | --- |
| 400 | 请求参数或请求体无效 |
| 401 | 未认证、token 无效或已过期 |
| 403 | 无权操作目标资源 |
| 404 | 资源不存在 |
| 409 | 资源冲突，例如用户名已存在 |
| 500 | 服务端内部错误 |

错误示例：

```json
{
  "code": 401,
  "msg": "invalid token",
  "data": null
}
```

## 公共接口

### 登录

```text
POST api/user/login
```

```json
{
  "username": "admin",
  "password": "123456"
}
```

### 注册

```text
POST api/user/register
```

```json
{
  "username": "tom",
  "password": "123456"
}
```

### 获取排行榜

```text
POST api/getTopNRecord
```

```json
{
  "gameType": 0,
  "topN": 10
}
```

返回数据为排行榜数组：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "username": "alice",
      "score": 25,
      "time": "2026-04-02-10:11:12.345",
      "avatarVersion": 11
    }
  ]
}
```

### 获取头像

```text
GET api/user/avatar/{username}
```

头像读取是公共资源，不携带用户 token。

## 鉴权接口

以下请求都必须携带：

```text
Authorization: Bearer <token>
```

### 校验当前会话

```text
GET api/user/checkToken
```

不再传递 `token` query 参数。成功返回：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "username": "admin",
    "uid": "1",
    "token": "token-string"
  }
}
```

### 修改密码

```text
POST api/user/changePassword?username=admin&oldPassword=123456&newPassword=654321
```

### 上传头像

```text
POST api/user/avatar?username=admin
Content-Type: multipart/form-data
```

文件字段名为 `file`。

### 新增游戏记录

```text
POST api/addGameRecord
```

```json
{
  "username": "admin",
  "gameType": 0,
  "score": 12,
  "time": "2026-04-02-10:11:12.345",
  "learnedWords": ["apple - 苹果", "book - 书"]
}
```

成功返回：

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 删除游戏记录

```text
POST api/deleteGameRecord
```

请求体使用 `username + gameType + score + time` 定位记录，成功时 `data` 为 `null`。

### 查询用户游戏记录

```text
POST api/getAllGameRecord
```

```json
{
  "username": "admin"
}
```

成功时 `data` 为游戏记录数组。

## 客户端网络层

Compose 页面不直接访问网络或本地数据。页面 Action 由 ViewModel 接收，
UseCase 再调用 Repository；网络层仍只负责 Retrofit/OkHttp 和统一错误处理。
导航、Snackbar、音频等一次性行为通过 ViewModel Effect 发送给 UI。

客户端按以下边界组织网络访问：

- `PublicAccountApi` / `PublicGameApi`：公共 Retrofit API。
- `AuthenticatedAccountApi` / `AuthenticatedGameApi`：鉴权 Retrofit API。
- `AccountRemoteDataSource` / `GameRecordRemoteDataSource`：封装 API 和统一错误处理。
- `LearningRemoteDataSource` / `LearningRepository`：服务端词库和学习状态的唯一业务入口，Room 只作为缓存与离线 mutation queue。
- `SessionManager`：唯一会话状态和 token 来源。

## 学习接口

学习接口均需携带 `Authorization: Bearer <token>`。服务端保存词库、掌握次数、每日目标和每日计划；客户端可以离线写入 mutation queue，联网后批量提交。

### 获取词库

```text
GET api/learning/catalog?query=abandon&mastered=false&page=0&size=100
```

返回 `items`、分页信息和 `catalogVersion`：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "english": "abandon",
        "pronunciation": "/əˈbændən/",
        "chinese": "v. 遗弃；放弃",
        "masterCount": 0,
        "mastered": false,
        "contentVersion": 1
      }
    ],
    "page": 0,
    "size": 100,
    "total": 1,
    "catalogVersion": 1
  }
}
```

### 获取学习状态

```text
GET api/learning/state
GET api/learning/plan
```

返回 `dailyTarget`、当天计划单词、完成数、未掌握数量、`syncVersion` 和用户进度。

### 更新每日目标

```text
PUT api/learning/settings/daily-target
```

```json
{
  "dailyTarget": 30
}
```

### 同步离线学习变更

```text
POST api/learning/sync
```

```json
{
  "mutations": [
    {
      "mutationId": "client-generated-uuid",
      "wordId": 1,
      "operation": "CORRECT_MATCH",
      "masterCount": null,
      "dailyTarget": null,
      "clientAt": "2026-08-09T08:00:00Z"
    }
  ]
}
```

服务端按 `mutationId` 幂等处理，返回已接受的 mutation ID 和最新学习状态。首次登录时客户端会把本地进度作为 `IMPORT_SNAPSHOT` 提交，服务端取更高掌握次数。
