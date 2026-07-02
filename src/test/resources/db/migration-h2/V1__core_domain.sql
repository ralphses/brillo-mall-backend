CREATE TABLE IF NOT EXISTS brillo_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    email VARCHAR(150),
    phone_number VARCHAR(20),
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    invited_by VARCHAR(100),
    referred_by VARCHAR(100),
    oauth2_user BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT uk_brillo_user_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_user_email UNIQUE (email),
    CONSTRAINT uk_brillo_user_phone UNIQUE (phone_number),
    CONSTRAINT uk_brillo_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS brillo_user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_brillo_user_roles_user FOREIGN KEY (user_id) REFERENCES brillo_user (id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON brillo_user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON brillo_user_roles (role);

CREATE TABLE IF NOT EXISTS brillo_business (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    customers LONGTEXT,
    slug VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    email VARCHAR(150),
    phone_number VARCHAR(20),
    logo_url VARCHAR(255),
    state VARCHAR(100),
    city VARCHAR(100),
    address TEXT,
    whatsapp_type VARCHAR(50) NOT NULL DEFAULT 'SHARED',
    whatsapp_number VARCHAR(20),
    storefront_name VARCHAR(100) NOT NULL DEFAULT 'Default',
    storefront_active BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    setup_completed BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_brillo_business_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_business_slug UNIQUE (slug),
    CONSTRAINT uk_brillo_business_email UNIQUE (email),
    CONSTRAINT uk_brillo_business_phone UNIQUE (phone_number),
    CONSTRAINT fk_brillo_business_owner FOREIGN KEY (owner_id) REFERENCES brillo_user (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_business_owner ON brillo_business (owner_id);
CREATE INDEX IF NOT EXISTS idx_brillo_business_slug ON brillo_business (slug);
CREATE INDEX IF NOT EXISTS idx_brillo_business_status ON brillo_business (status);

CREATE TABLE IF NOT EXISTS brillo_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_customer_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_customer_email UNIQUE (email),
    CONSTRAINT uk_brillo_customer_phone UNIQUE (phone_number)
);

CREATE INDEX IF NOT EXISTS idx_brillo_customer_user ON brillo_customer (user_id);

CREATE TABLE IF NOT EXISTS customer_related_business_ids (
    customer_id BIGINT NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_customer_related_business_ids PRIMARY KEY (customer_id, business_id),
    CONSTRAINT fk_customer_related_business_ids_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (id),
    CONSTRAINT fk_customer_related_business_ids_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference)
);

CREATE INDEX IF NOT EXISTS idx_customer_related_business_ids_business ON customer_related_business_ids (business_id);

CREATE TABLE IF NOT EXISTS brillo_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    discounted_price DECIMAL(10,2),
    sku VARCHAR(50),
    main_image_url VARCHAR(200),
    quantity INT,
    status VARCHAR(100) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_product_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_product_business_sku UNIQUE (business_id, sku),
    CONSTRAINT uk_brillo_product_business_name UNIQUE (business_id, name),
    CONSTRAINT fk_brillo_product_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_product_business ON brillo_product (business_id);
CREATE INDEX IF NOT EXISTS idx_brillo_product_status ON brillo_product (status);

CREATE TABLE IF NOT EXISTS brillo_business_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    pricing_type VARCHAR(50),
    base_price DECIMAL(19,2),
    duration_minutes INT,
    negotiable BOOLEAN NOT NULL DEFAULT FALSE,
    requires_schedule BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_business_service_reference UNIQUE (reference),
    CONSTRAINT uk_business_service_slug UNIQUE (business_id, slug),
    CONSTRAINT fk_brillo_business_service_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference)
);

CREATE INDEX IF NOT EXISTS idx_service_business_id ON brillo_business_service (business_id);
CREATE INDEX IF NOT EXISTS idx_service_slug ON brillo_business_service (slug);
CREATE INDEX IF NOT EXISTS idx_service_status ON brillo_business_service (status);

CREATE TABLE IF NOT EXISTS brillo_business_service_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    whatsapp_conversation_id VARCHAR(100),
    business_service_id VARCHAR(36) NOT NULL,
    initial_price DECIMAL(19,2),
    last_offered_price DECIMAL(19,2),
    agreed_price DECIMAL(19,2),
    negotiation_attempts INT DEFAULT 0,
    human_takeover BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    request_status VARCHAR(50) NOT NULL DEFAULT 'NEGOTIATING',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_business_service_request_reference UNIQUE (reference),
    CONSTRAINT fk_brillo_business_service_request_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (reference),
    CONSTRAINT fk_brillo_business_service_request_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference),
    CONSTRAINT fk_brillo_business_service_request_service FOREIGN KEY (business_service_id) REFERENCES brillo_business_service (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_business_service_request_business ON brillo_business_service_request (business_id);
CREATE INDEX IF NOT EXISTS idx_brillo_business_service_request_service ON brillo_business_service_request (business_service_id);
CREATE INDEX IF NOT EXISTS idx_brillo_business_service_request_user ON brillo_business_service_request (user_id);
CREATE INDEX IF NOT EXISTS idx_brillo_business_service_request_conversation ON brillo_business_service_request (whatsapp_conversation_id);

CREATE TABLE IF NOT EXISTS brillo_booked_business_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    business_service_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    service_request_id VARCHAR(36),
    agreed_price DECIMAL(19,2) NOT NULL,
    scheduled_date TIMESTAMP,
    booking_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_brillo_booked_business_service_reference UNIQUE (reference),
    CONSTRAINT fk_brillo_booked_business_service_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference),
    CONSTRAINT fk_brillo_booked_business_service_service FOREIGN KEY (business_service_id) REFERENCES brillo_business_service (reference),
    CONSTRAINT fk_brillo_booked_business_service_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (reference),
    CONSTRAINT fk_brillo_booked_business_service_request FOREIGN KEY (service_request_id) REFERENCES brillo_business_service_request (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_booked_business_service_business ON brillo_booked_business_service (business_id);
CREATE INDEX IF NOT EXISTS idx_brillo_booked_business_service_service ON brillo_booked_business_service (business_service_id);
CREATE INDEX IF NOT EXISTS idx_brillo_booked_business_service_user ON brillo_booked_business_service (user_id);
CREATE INDEX IF NOT EXISTS idx_brillo_booked_business_service_customer ON brillo_booked_business_service (customer_id);

CREATE TABLE IF NOT EXISTS brillo_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(36),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address TEXT,
    CONSTRAINT uk_brillo_order_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_order_order_id UNIQUE (order_id),
    CONSTRAINT fk_brillo_order_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference),
    CONSTRAINT fk_brillo_order_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_order_business ON brillo_order (business_id);
CREATE INDEX IF NOT EXISTS idx_brillo_order_customer ON brillo_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_brillo_order_user ON brillo_order (user_id);
CREATE INDEX IF NOT EXISTS idx_brillo_order_status ON brillo_order (status);

CREATE TABLE IF NOT EXISTS brillo_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10,2) NOT NULL,
    CONSTRAINT uk_brillo_order_item_reference UNIQUE (reference),
    CONSTRAINT fk_brillo_order_item_order FOREIGN KEY (order_id) REFERENCES brillo_order (id),
    CONSTRAINT fk_brillo_order_item_product FOREIGN KEY (product_id) REFERENCES brillo_product (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_order_item_order ON brillo_order_item (order_id);
CREATE INDEX IF NOT EXISTS idx_brillo_order_item_product ON brillo_order_item (product_id);

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    whatsapp_conversation_id VARCHAR(100),
    last_interaction_at TIMESTAMP,
    CONSTRAINT uk_conversations_reference UNIQUE (reference),
    CONSTRAINT uk_conversations_whatsapp_conversation_id UNIQUE (whatsapp_conversation_id),
    CONSTRAINT fk_conversations_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference),
    CONSTRAINT fk_conversations_customer FOREIGN KEY (customer_id) REFERENCES brillo_customer (reference)
);

CREATE INDEX IF NOT EXISTS idx_conversations_business_id ON conversations (business_id);
CREATE INDEX IF NOT EXISTS idx_conversations_customer_id ON conversations (customer_id);
CREATE INDEX IF NOT EXISTS idx_conversations_whatsapp_conversation_id ON conversations (whatsapp_conversation_id);

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    conversation_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    intent VARCHAR(100),
    CONSTRAINT uk_messages_reference UNIQUE (reference),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id)
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages (conversation_id);

CREATE TABLE IF NOT EXISTS brillo_payment_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    business_id VARCHAR(36) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payable_type VARCHAR(50) NOT NULL,
    payable_id VARCHAR(36) NOT NULL,
    CONSTRAINT uk_brillo_payment_log_reference UNIQUE (reference),
    CONSTRAINT uk_brillo_payment_log_payment_reference UNIQUE (payment_reference),
    CONSTRAINT fk_brillo_payment_log_business FOREIGN KEY (business_id) REFERENCES brillo_business (reference)
);

CREATE INDEX IF NOT EXISTS idx_brillo_payment_log_reference ON brillo_payment_log (reference);
CREATE INDEX IF NOT EXISTS idx_brillo_payment_log_business ON brillo_payment_log (business_id);
CREATE INDEX IF NOT EXISTS idx_brillo_payment_log_payable ON brillo_payment_log (payable_id);

CREATE TABLE IF NOT EXISTS brillo_all_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    request_body LONGTEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    response_body LONGTEXT,
    origin VARCHAR(100),
    responded_at TIMESTAMP,
    CONSTRAINT uk_request_reference UNIQUE (reference)
);

CREATE INDEX IF NOT EXISTS idx_request_reference ON brillo_all_request (reference);
CREATE INDEX IF NOT EXISTS idx_request_status ON brillo_all_request (status);

CREATE TABLE IF NOT EXISTS media_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    reference VARCHAR(36) NOT NULL,
    url VARCHAR(255),
    asset_id VARCHAR(255),
    name VARCHAR(255),
    CONSTRAINT uk_media_asset_reference UNIQUE (reference)
);

CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    completion_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS event_publication_archive (
    id UUID PRIMARY KEY,
    publication_date TIMESTAMP NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    serialized_event VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    completion_date TIMESTAMP
);
