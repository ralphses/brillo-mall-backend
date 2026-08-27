SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'reopen_count'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN reopen_count INT NOT NULL DEFAULT 0'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'last_reopened_at'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN last_reopened_at TIMESTAMP NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'last_session_event'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN last_session_event VARCHAR(50) NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'last_session_event_at'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN last_session_event_at TIMESTAMP NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND index_name = 'idx_conversations_last_reopened_at'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversations_last_reopened_at ON conversations (last_reopened_at)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
