package com.clickstechnology.Brillo.Mall.domain.business;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class BusinessSpecification {

    Specification<Business> search(String search) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("storefrontName")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("category").as(String.class)), likePattern)
                ));
            }

            predicates.add(criteriaBuilder.equal(root.get("status"), EntityStatus.ACTIVE));
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
