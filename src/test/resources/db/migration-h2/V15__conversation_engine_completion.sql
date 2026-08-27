ALTER TABLE conversations ADD COLUMN IF NOT EXISTS reopen_count INT NOT NULL DEFAULT 0;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_reopened_at TIMESTAMP NULL;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_session_event VARCHAR(50) NULL;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_session_event_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_conversations_last_reopened_at
    ON conversations (last_reopened_at);
