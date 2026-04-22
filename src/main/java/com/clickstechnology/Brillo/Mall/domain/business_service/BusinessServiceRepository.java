package com.clickstechnology.Brillo.Mall.domain.business_service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface BusinessServiceRepository extends JpaRepository<BusinessService, Long> {
    boolean existsByBusinessIdAndSlug(String businessId, String slug);

    Page<BusinessService> findByBusinessId(String businessId, Pageable pageable);

    @Query("SELECT b FROM BusinessService b WHERE b.reference = :ref")
    Optional<BusinessService> findByServiceId(@Param("ref") String businessId);

    @Query("SELECT b FROM BusinessService b " +
            "WHERE b.reference = :ref OR b.slug = :ref")
    Optional<BusinessService> findByServiceIdOrSlug(@Param("ref") String ref);


}