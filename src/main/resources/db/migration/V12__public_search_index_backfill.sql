INSERT INTO brillo_public_search_index (
    record_status,
    created_at,
    updated_at,
    created_by,
    updated_by,
    reference,
    item_type,
    business_id,
    name,
    description,
    category_code,
    category_label,
    business_name,
    slug,
    sku,
    image_url,
    location,
    price,
    search_text,
    is_active,
    status
)
SELECT
    'ACTIVE',
    b.created_at,
    b.updated_at,
    b.created_by,
    b.updated_by,
    b.reference,
    'BUSINESS',
    b.reference,
    b.name,
    b.description,
    b.category,
    b.category,
    NULL,
    b.slug,
    NULL,
    b.logo_url,
    CASE
        WHEN b.city IS NULL AND b.state IS NULL THEN NULL
        ELSE CONCAT_WS(', ', b.city, b.state)
    END,
    NULL,
    LOWER(CONCAT_WS(' ',
        b.name,
        b.storefront_name,
        b.description,
        b.slug,
        b.city,
        b.state,
        b.category
    )),
    TRUE,
    b.status
FROM brillo_business b
WHERE b.status = 'ACTIVE'
  AND b.is_active = TRUE
  AND b.storefront_active = TRUE
  AND b.record_status <> 'DELETED';

INSERT INTO brillo_public_search_index (
    record_status,
    created_at,
    updated_at,
    created_by,
    updated_by,
    reference,
    item_type,
    business_id,
    name,
    description,
    category_code,
    category_label,
    business_name,
    slug,
    sku,
    image_url,
    location,
    price,
    search_text,
    is_active,
    status
)
SELECT
    'ACTIVE',
    p.created_at,
    p.updated_at,
    p.created_by,
    p.updated_by,
    p.reference,
    'PRODUCT',
    p.business_id,
    p.name,
    p.description,
    p.category,
    COALESCE(pc.label, p.category),
    COALESCE(b.storefront_name, b.name),
    NULL,
    p.sku,
    p.main_image_url,
    CASE
        WHEN b.city IS NULL AND b.state IS NULL THEN NULL
        ELSE CONCAT_WS(', ', b.city, b.state)
    END,
    p.price,
    LOWER(CONCAT_WS(' ',
        p.name,
        p.description,
        p.sku,
        p.category,
        COALESCE(pc.label, p.category),
        b.name,
        b.storefront_name,
        b.slug,
        b.city,
        b.state,
        b.description
    )),
    TRUE,
    p.status
FROM brillo_product p
INNER JOIN brillo_business b ON b.reference = p.business_id
LEFT JOIN brillo_category_catalog pc
       ON pc.category_type = 'PRODUCTS'
      AND UPPER(pc.code) = UPPER(p.category)
      AND pc.record_status = 'ACTIVE'
WHERE p.status = 'ACTIVE'
  AND p.record_status <> 'DELETED'
  AND b.status = 'ACTIVE'
  AND b.is_active = TRUE
  AND b.storefront_active = TRUE
  AND b.record_status <> 'DELETED';

INSERT INTO brillo_public_search_index (
    record_status,
    created_at,
    updated_at,
    created_by,
    updated_by,
    reference,
    item_type,
    business_id,
    name,
    description,
    category_code,
    category_label,
    business_name,
    slug,
    sku,
    image_url,
    location,
    price,
    search_text,
    is_active,
    status
)
SELECT
    'ACTIVE',
    s.created_at,
    s.updated_at,
    s.created_by,
    s.updated_by,
    s.reference,
    'SERVICE',
    s.business_id,
    s.name,
    s.description,
    s.category,
    COALESCE(sc.label, s.category),
    COALESCE(b.storefront_name, b.name),
    s.slug,
    NULL,
    b.logo_url,
    CASE
        WHEN b.city IS NULL AND b.state IS NULL THEN NULL
        ELSE CONCAT_WS(', ', b.city, b.state)
    END,
    s.base_price,
    LOWER(CONCAT_WS(' ',
        s.name,
        s.description,
        s.slug,
        s.category,
        COALESCE(sc.label, s.category),
        b.name,
        b.storefront_name,
        b.slug,
        b.city,
        b.state,
        b.description
    )),
    TRUE,
    s.status
FROM brillo_business_service s
INNER JOIN brillo_business b ON b.reference = s.business_id
LEFT JOIN brillo_category_catalog sc
       ON sc.category_type = 'SERVICES'
      AND UPPER(sc.code) = UPPER(s.category)
      AND sc.record_status = 'ACTIVE'
WHERE s.status = 'ACTIVE'
  AND s.is_active = TRUE
  AND s.record_status <> 'DELETED'
  AND b.status = 'ACTIVE'
  AND b.is_active = TRUE
  AND b.storefront_active = TRUE
  AND b.record_status <> 'DELETED';
