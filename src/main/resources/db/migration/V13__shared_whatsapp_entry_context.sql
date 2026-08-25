SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'conversations'
              AND column_name = 'entry_business_id'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN entry_business_id VARCHAR(36) NULL'
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
              AND column_name = 'active_business_id'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN active_business_id VARCHAR(36) NULL'
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
              AND column_name = 'entry_slug'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN entry_slug VARCHAR(150) NULL'
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
              AND column_name = 'marketplace_mode'
        ),
        'SELECT 1',
        'ALTER TABLE conversations ADD COLUMN marketplace_mode BOOLEAN NOT NULL DEFAULT FALSE'
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
              AND column_name = 'business_id'
              AND is_nullable = 'YES'
        ),
        'SELECT 1',
        'ALTER TABLE conversations MODIFY COLUMN business_id VARCHAR(36) NULL'
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
              AND index_name = 'idx_conversations_entry_business_id'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversations_entry_business_id ON conversations (entry_business_id)'
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
              AND index_name = 'idx_conversations_active_business_id'
        ),
        'SELECT 1',
        'CREATE INDEX idx_conversations_active_business_id ON conversations (active_business_id)'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
