package com.clickstechnology.Brillo.Mall.domain.business_service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

interface BookedBusinessServiceRepository extends JpaRepository<BookedBusinessService, Long> {

    Page<BookedBusinessService> findAllByBusinessServiceId(String businessServiceId, Pageable pageable);
    Page<BookedBusinessService> findAllByBusinessId(String businessId, Pageable pageable);
    Page<BookedBusinessService> findAllByUserId(String userId, Pageable pageable);
    Page<BookedBusinessService> findAllByBusinessIdIn(Set<String> businessIds, Pageable pageable);

    Optional<BookedBusinessService> findByReference(String reference);
}
