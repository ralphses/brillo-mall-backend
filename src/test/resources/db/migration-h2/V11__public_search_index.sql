CREATE TABLE IF NOT EXISTS brillo_public_search_index (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    business_id VARCHAR(36),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category_code VARCHAR(100),
    category_label VARCHAR(150),
    business_name VARCHAR(150),
    slug VARCHAR(150),
    sku VARCHAR(50),
    image_url VARCHAR(255),
    location VARCHAR(255),
    price DECIMAL(19,2),
    search_text CLOB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_public_search_reference_type UNIQUE (reference, item_type)
);

CREATE INDEX IF NOT EXISTS idx_public_search_visibility ON brillo_public_search_index (status, is_active, created_at);
CREATE INDEX IF NOT EXISTS idx_public_search_type ON brillo_public_search_index (item_type);
CREATE INDEX IF NOT EXISTS idx_public_search_business ON brillo_public_search_index (business_id);
CREATE INDEX IF NOT EXISTS idx_public_search_category ON brillo_public_search_index (category_code);
