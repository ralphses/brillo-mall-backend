package com.clickstechnology.Brillo.Mall.domain.business_service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface BusinessServiceRequestRepository extends JpaRepository<BusinessServiceRequest, Long> {

    Optional<BusinessServiceRequest> findByReference(String reference);

    @Query("SELECT req FROM BusinessServiceRequest req " +
            "WHERE req.businessId = ?1 " +
            "AND req.businessServiceId = ?2")
    Page<BusinessServiceRequest> findByBusinessIdAndBusinessServiceId(String businessId, String businessServiceId, Pageable pageable);

    Page<BusinessServiceRequest> findByBusinessId(String businessId, Pageable pageable);

    @Query("SELECT req FROM BusinessServiceRequest req " +
            "WHERE req.businessServiceId = ?1")
    Page<BusinessServiceRequest> findByBusinessServiceId(String businessServiceId, Pageable pageable);

    @Query("SELECT req FROM BusinessServiceRequest req " +
            "WHERE req.userId = ?1")
    Page<BusinessServiceRequest> findByUserId(String userId, Pageable pageable);
}