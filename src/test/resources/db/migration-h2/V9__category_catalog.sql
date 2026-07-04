CREATE TABLE IF NOT EXISTS brillo_category_catalog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    code VARCHAR(100) NOT NULL,
    label VARCHAR(150) NOT NULL,
    category_type VARCHAR(20) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_brillo_category_catalog_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_category_catalog_type_code UNIQUE (category_type, code)
);

CREATE INDEX IF NOT EXISTS idx_brillo_category_catalog_type ON brillo_category_catalog (category_type);
CREATE INDEX IF NOT EXISTS idx_brillo_category_catalog_code ON brillo_category_catalog (code);

ALTER TABLE brillo_product ADD COLUMN IF NOT EXISTS category VARCHAR(100);

INSERT INTO brillo_category_catalog (
    record_status,
    created_at,
    updated_at,
    reference,
    code,
    label,
    category_type,
    display_order
) VALUES
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe001', 'PHARMACY', 'Pharmacy', 'PRODUCTS', 1),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe002', 'GROCERY', 'Grocery', 'PRODUCTS', 2),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe003', 'FASHION', 'Fashion', 'PRODUCTS', 3),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe004', 'ELECTRONICS', 'Electronics', 'PRODUCTS', 4),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe005', 'HOME', 'Home', 'PRODUCTS', 5),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe006', 'BEAUTY', 'Beauty', 'PRODUCTS', 6),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe101', 'PHARMACY', 'Pharmacy', 'SERVICES', 1),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe102', 'GROCERY', 'Grocery', 'SERVICES', 2),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe103', 'FASHION', 'Fashion', 'SERVICES', 3),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe104', 'ELECTRONICS', 'Electronics', 'SERVICES', 4),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe105', 'HOME', 'Home', 'SERVICES', 5),
    ('ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '7f6f4f47-0dd6-4b34-95cf-0c3ff0bfe106', 'BEAUTY', 'Beauty', 'SERVICES', 6);
