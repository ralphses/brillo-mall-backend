ALTER TABLE conversations ADD COLUMN IF NOT EXISTS entry_business_id VARCHAR(36);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS active_business_id VARCHAR(36);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS entry_slug VARCHAR(150);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS marketplace_mode BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE conversations ALTER COLUMN business_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_conversations_entry_business_id ON conversations (entry_business_id);
CREATE INDEX IF NOT EXISTS idx_conversations_active_business_id ON conversations (active_business_id);
