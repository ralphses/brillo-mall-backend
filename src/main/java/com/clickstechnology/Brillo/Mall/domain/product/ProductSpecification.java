package com.clickstechnology.Brillo.Mall.domain.product;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ProductSpecification {

    public Specification<Product> getProducts(String businessId) {
        return (root, query, criteriaBuilder) -> {
            if (businessId != null && !businessId.trim().isEmpty()) {
                return criteriaBuilder.equal(root.get("businessId"), businessId);
            } else {
                return criteriaBuilder.conjunction();
            }
        };
    }
}