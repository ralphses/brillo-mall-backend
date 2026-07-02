package com.clickstechnology.Brillo.Mall.domain.product;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSpecification {

    public Specification<Product> getProducts(
            String businessId,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly,
            EntityStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (businessId != null && !businessId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), businessId));
            }

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
                ));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), 0));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
