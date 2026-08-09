# WordCrush 数据库与缓存设计

## 设计目标

- 满足 Android 客户端当前全部后端接口
- 由服务端统一维护词库、掌握进度和每日学习计划
- 覆盖可写进简历的工程能力：结构化建模、缓存化登录态、容器化部署、迁移脚本
- 当前为开发阶段，schema 与接口允许直接演进，不额外维护新旧兼容层

## MySQL 表设计

### `users`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键，自增 |
| `username` | `VARCHAR(32)` | 用户名，唯一索引 |
| `password_hash` | `VARCHAR(100)` | BCrypt 密码哈希 |
| `status` | `TINYINT` | 账号状态 |
| `created_at` | `DATETIME(3)` | 创建时间 |
| `updated_at` | `DATETIME(3)` | 更新时间 |

索引：

- `uk_users_username (username)`

### `game_records`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键，自增 |
| `user_id` | `BIGINT` | 用户外键 |
| `game_type` | `TINYINT` | 游戏类型，`0=Classic`，`1=Timed` |
| `score` | `INT` | 单局得分 |
| `played_at` | `DATETIME(3)` | 客户端上传的局时间 |
| `created_at` | `DATETIME(3)` | 创建时间 |
| `updated_at` | `DATETIME(3)` | 更新时间 |

索引与约束：

- `fk_game_records_user`
- `uk_game_records_record (user_id, game_type, score, played_at)`
- `idx_game_records_user_time (user_id, played_at DESC)`
- `idx_game_records_type_score (game_type, score DESC, played_at ASC)`

### `game_record_words`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键，自增 |
| `game_record_id` | `BIGINT` | 战绩外键 |
| `sort_order` | `INT` | learned words 顺序 |
| `word_content` | `VARCHAR(255)` | 单词摘要内容 |
| `created_at` | `DATETIME(3)` | 创建时间 |
| `updated_at` | `DATETIME(3)` | 更新时间 |

索引与约束：

- `fk_game_record_words_record`
- `idx_game_record_words_record (game_record_id, sort_order)`

### `learning_words`

服务端词库由 `src/main/resources/wordbook.csv` 初始化，使用 CSV 的稳定 ID。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `INT` | 词条稳定 ID |
| `english` | `VARCHAR(128)` | 英文单词 |
| `pronunciation` | `VARCHAR(255)` | 音标 |
| `chinese` | `VARCHAR(1024)` | 中文释义 |
| `content_version` | `BIGINT` | 内容版本 |
| `status` | `TINYINT` | 是否启用 |

### `user_word_progress`

按用户和词条保存掌握次数与掌握状态；`master_count` 上限为 3。

### `user_learning_settings` / `user_daily_plans` / `user_daily_plan_items`

保存用户每日目标、按日期生成的学习计划和计划中的词条顺序。计划优先选择未掌握词条，并保持同一用户同一天的计划稳定。

### `learning_sync_mutations`

保存客户端离线提交的 `mutation_id`，以 `(user_id, mutation_id)` 保证幂等。支持正确匹配、标记未掌握、首次登录进度快照和每日目标更新。

## Redis 设计

### Key 约定

- `wordcrush:auth:token:{token}`
  - 值：token session JSON
  - 作用：快速校验登录态
  - TTL：默认 168 小时

- `wordcrush:auth:user:{userId}`
  - 值：用户持有 token 集合
  - 作用：修改密码后批量失效会话

## 设计说明

- MySQL 存放强一致业务数据
- Redis 承担会话缓存和 token 索引
- Flyway 负责数据库版本演进
- Docker Compose 负责完整部署交付
