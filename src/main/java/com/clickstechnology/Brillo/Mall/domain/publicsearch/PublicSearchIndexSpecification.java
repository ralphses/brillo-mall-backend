package com.clickstechnology.Brillo.Mall.domain.publicsearch;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PublicSearchIndexSpecification {

    public Specification<PublicSearchIndex> search(String search) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), EntityStatus.ACTIVE));
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(root.get("searchText"), likePattern));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
