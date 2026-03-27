package com.clickstechnology.Brillo.Mall.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByBusinessIdAndNameIgnoreCase(String businessId, String name);

    boolean existsByBusinessIdAndSkuIgnoreCase(String businessId, String sku);

    Optional<Product> findByReference(String reference);

    Optional<Product> findBySku(String sku);

    List<Product> findAllByReferenceIn(List<String> references);
}