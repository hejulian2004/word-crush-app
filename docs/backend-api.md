# WordCrush 后端接口文档

本文档描述当前客户端和 sibling Spring Boot 服务端共同使用的接口契约。

## 基本信息

- Base URL：`https://txy.hejulian.org/word-crush/`
- HTTP 客户端：Retrofit + OkHttp + Gson
- WebSocket Base URL：`wss://txy.hejulian.org/word-crush/`
- 鉴权方式：`Authorization: Bearer <token>`
- 不再支持 `token` 请求头或 query token

## 统一响应

所有 JSON API 返回：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

- `code == 200` 表示业务成功。
- 失败请求使用对应 HTTP 状态码，并返回相同结构。
- `data` 可以是对象、数组、字符串或 `null`。

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
  "data": "ok"
}
```

### 删除游戏记录

```text
POST api/deleteGameRecord
```

请求体使用 `username + gameType + score + time` 定位记录，成功时 `data` 为 `deleted`。

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
- `SessionManager`：唯一会话状态和 token 来源。
- `SocketClient`：OkHttp WebSocket 长连接基础设施。

当前后端尚未提供具体 WebSocket 业务端点，客户端只保留通用传输能力。
