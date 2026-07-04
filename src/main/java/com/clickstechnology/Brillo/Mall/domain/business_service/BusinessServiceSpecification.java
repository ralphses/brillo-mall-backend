package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class BusinessServiceSpecification {

    Specification<BusinessService> search(String search) {
        return getServices(null, search, null, null, null, null, EntityStatus.ACTIVE, Boolean.TRUE);
    }

    Specification<BusinessService> getServices(
            String businessId,
            String search,
            String category,
            PricingType pricingType,
            Boolean negotiable,
            Boolean requiresSchedule,
            EntityStatus status,
            Boolean active) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (businessId != null && !businessId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), businessId));
            }

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), likePattern)
                ));
            }

            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category")),
                        category.trim().toLowerCase()));
            }

            if (pricingType != null) {
                predicates.add(criteriaBuilder.equal(root.get("pricingType"), pricingType));
            }

            if (negotiable != null) {
                predicates.add(criteriaBuilder.equal(root.get("negotiable"), negotiable));
            }

            if (requiresSchedule != null) {
                predicates.add(criteriaBuilder.equal(root.get("requiresSchedule"), requiresSchedule));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
