ALTER TABLE conversations ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS last_intent VARCHAR(100);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS active_task_key VARCHAR(100);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS human_takeover BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS session_expires_at TIMESTAMP;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS whatsapp_business_number VARCHAR(30);

CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations (status);
CREATE INDEX IF NOT EXISTS idx_conversations_session_expires_at ON conversations (session_expires_at);

ALTER TABLE messages ADD COLUMN IF NOT EXISTS whatsapp_message_id VARCHAR(100);
ALTER TABLE messages ADD COLUMN IF NOT EXISTS transport_type VARCHAR(50);
ALTER TABLE messages ADD COLUMN IF NOT EXISTS source_event_id VARCHAR(100);
ALTER TABLE messages ADD COLUMN IF NOT EXISTS metadata TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uk_messages_whatsapp_message_id ON messages (whatsapp_message_id);
CREATE INDEX IF NOT EXISTS idx_messages_source_event_id ON messages (source_event_id);
