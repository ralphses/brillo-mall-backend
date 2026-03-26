package com.clickstechnology.Brillo.Mall.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByBusinessIdAndNameIgnoreCase(String businessId, String name);
    boolean existsByBusinessIdAndSkuIgnoreCase(String businessId, String sku);
}