CREATE TABLE learning_words (
    id INT NOT NULL PRIMARY KEY,
    english VARCHAR(128) NOT NULL,
    pronunciation VARCHAR(255) NOT NULL,
    chinese VARCHAR(1024) NOT NULL,
    content_version BIGINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_learning_words_status_id (status, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_word_progress (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    word_id INT NOT NULL,
    master_count TINYINT NOT NULL DEFAULT 0,
    is_mastered TINYINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_user_word_progress_user_word UNIQUE (user_id, word_id),
    CONSTRAINT fk_user_word_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_word_progress_word FOREIGN KEY (word_id) REFERENCES learning_words (id) ON DELETE CASCADE,
    INDEX idx_user_word_progress_user_updated (user_id, updated_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_learning_settings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    daily_target INT NOT NULL DEFAULT 30,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_user_learning_settings_user UNIQUE (user_id),
    CONSTRAINT fk_user_learning_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_daily_plans (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_date DATE NOT NULL,
    daily_target INT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_user_daily_plans_user_date UNIQUE (user_id, plan_date),
    CONSTRAINT fk_user_daily_plans_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_daily_plan_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    word_id INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_user_daily_plan_items_plan_word UNIQUE (plan_id, word_id),
    CONSTRAINT fk_user_daily_plan_items_plan FOREIGN KEY (plan_id) REFERENCES user_daily_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_daily_plan_items_word FOREIGN KEY (word_id) REFERENCES learning_words (id) ON DELETE CASCADE,
    INDEX idx_user_daily_plan_items_plan_order (plan_id, sort_order)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE learning_sync_mutations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mutation_id VARCHAR(96) NOT NULL,
    word_id INT NULL,
    operation VARCHAR(32) NOT NULL,
    master_count TINYINT NULL,
    daily_target INT NULL,
    client_at VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_learning_sync_mutations_user_mutation UNIQUE (user_id, mutation_id),
    CONSTRAINT fk_learning_sync_mutations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_sync_mutations_word FOREIGN KEY (word_id) REFERENCES learning_words (id) ON DELETE CASCADE,
    INDEX idx_learning_sync_mutations_user_created (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
