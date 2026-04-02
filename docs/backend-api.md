# WordCrush 后端接口文档

本文档以当前 Android 客户端实际调用的接口为准，整理了客户端所需的请求路径、请求体、返回结构和字段说明。

## 基本信息

- 默认 Base URL：`http://192.168.201.21:8080`
- 客户端 HTTP 框架：`Retrofit + Gson`
- 鉴权方式：当前客户端没有统一的 `Authorization` Header，账号相关接口使用 query/body 传参

说明：
- `login` 和 `checkToken` 接口仍保留在客户端 API 定义里，但当前默认登录流程由本地管理员账号接管
- 如果以后恢复真实后端登录，这两个接口可以直接继续使用

---

## 一、账号接口

账号接口定义见：

- [AccountApi.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/api/AccountApi.kt)
- [ApiResponse.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/model/ApiResponse.kt)

### 1. 登录

- 方法：`POST`
- 路径：`/api/user/login`
- 请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

请求模型：

```json
{
  "username": "string",
  "password": "string"
}
```

返回模型：

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

字段说明：

- `code == 200` 代表成功
- `msg` 为提示信息
- `data.username` 用户名
- `data.uid` 用户 ID
- `data.token` 登录 token

### 2. 校验 token

- 方法：`GET`
- 路径：`/api/user/checkToken`
- Query 参数：
  - `token`

示例：

```text
GET /api/user/checkToken?token=token-string
```

返回模型：

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

### 3. 注册

- 方法：`POST`
- 路径：`/api/user/register`
- 请求体：

```json
{
  "username": "string",
  "password": "string"
}
```

返回模型：

```json
{
  "code": 200,
  "msg": "register success",
  "data": {
    "username": "string",
    "uid": "string",
    "token": "string"
  }
}
```

说明：

- 客户端当前只使用 `msg` 判断注册提示
- 如果后端注册成功后不返回 token，也建议至少保持 `code/msg/data` 结构一致

### 4. 修改密码

- 方法：`POST`
- 路径：`/api/user/changePassword`
- Query 参数：
  - `username`
  - `oldPassword`
  - `newPassword`

示例：

```text
POST /api/user/changePassword?username=admin&oldPassword=123456&newPassword=654321
```

返回模型：

```json
{
  "code": 200,
  "msg": "password changed",
  "data": null
}
```

---

## 二、游戏记录与排行榜接口

接口定义见：

- [GameRecordApi.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/api/GameRecordApi.kt)
- [GameRecordRemoteModels.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/model/GameRecordRemoteModels.kt)

这一组接口使用的是另一套旧响应格式：

```json
{
  "status": "success",
  "message": ...
}
```

客户端判定成功条件：

- `status == "success"`

### 1. 获取排行榜

- 方法：`POST`
- 路径：`/api/getTopNRecord`
- 请求体：

```json
{
  "gameType": 0,
  "topN": 10
}
```

字段说明：

- `gameType`
  - `0` = Match / Classic
  - `1` = Timed
- `topN` 返回前 N 条记录

成功返回示例：

```json
{
  "status": "success",
  "message": [
    {
      "username": "alice",
      "score": 25,
      "time": "2026-04-02-10:11:12.345"
    }
  ]
}
```

### 2. 新增游戏记录

- 方法：`POST`
- 路径：`/api/addGameRecord`
- 请求体：

```json
{
  "username": "admin",
  "gameType": 0,
  "score": 12,
  "time": "2026-04-02-10:11:12.345",
  "learnedWords": [
    "apple - 苹果",
    "book - 书"
  ]
}
```

字段说明：

- `username` 用户名
- `gameType` 游戏类型
- `score` 本局得分
- `time` 时间字符串，格式由客户端生成：`yyyy-MM-dd-HH:mm:ss.SSS`
- `learnedWords` 本局学到的单词摘要列表

成功返回示例：

```json
{
  "status": "success",
  "message": "ok"
}
```

### 3. 删除游戏记录

- 方法：`POST`
- 路径：`/api/deleteGameRecord`
- 请求体：

```json
{
  "username": "admin",
  "gameType": 0,
  "score": 12,
  "time": "2026-04-02-10:11:12.345"
}
```

说明：

- 客户端通过 `username + gameType + score + time` 定位一条记录
- 后端删除接口也需要按这组字段删除

成功返回示例：

```json
{
  "status": "success",
  "message": "deleted"
}
```

### 4. 获取某个用户的全部游戏记录

- 方法：`POST`
- 路径：`/api/getAllGameRecord`
- 请求体：

```json
{
  "username": "admin"
}
```

成功返回示例：

```json
{
  "status": "success",
  "message": [
    {
      "username": "admin",
      "gameType": 0,
      "score": 12,
      "time": "2026-04-02-10:11:12.345",
      "learnedWords": [
        "apple - 苹果",
        "book - 书"
      ]
    }
  ]
}
```

字段说明：

- `message` 为记录数组
- 每条记录字段：
  - `username`
  - `gameType`
  - `score`
  - `time`
  - `learnedWords`

---

## 三、客户端当前依赖关系

### 强依赖后端的接口

- `/api/user/register`
- `/api/user/changePassword`
- `/api/getTopNRecord`
- `/api/addGameRecord`
- `/api/deleteGameRecord`
- `/api/getAllGameRecord`

### 当前保留但默认未走后端的接口

- `/api/user/login`
- `/api/user/checkToken`

原因：

- 当前客户端默认使用本地管理员账号 `admin / 123456`
- 登录和 token 校验在 `AccountRepository` 中被本地逻辑接管
- 如果恢复真实账号系统，上述两个接口即可重新启用

---

## 四、建议统一响应格式

当前客户端里实际上存在两套后端响应格式：

### 账号接口

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

### 记录接口

```json
{
  "status": "success",
  "message": {}
}
```

建议后端后续统一为一套响应格式，降低客户端维护成本。例如：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

---

## 五、实现参考

如需核对客户端实际字段，请直接查看：

- [AccountApi.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/api/AccountApi.kt)
- [GameRecordApi.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/api/GameRecordApi.kt)
- [ApiResponse.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/model/ApiResponse.kt)
- [GameRecordRemoteModels.kt](E:/coding/word-crush-app/app/src/main/java/com/example/wordcrush/data/model/GameRecordRemoteModels.kt)
