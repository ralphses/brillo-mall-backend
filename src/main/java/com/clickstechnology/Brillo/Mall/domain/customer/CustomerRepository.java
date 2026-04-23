package com.clickstechnology.Brillo.Mall.domain.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByName(String name);
    Optional<Customer> findByEmail(String email);

    @Query("SELECT customer FROM Customer customer " +
            "LEFT JOIN FETCH customer.relatedBusinessIds " +
            "WHERE customer.phone = ?1")
    Optional<Customer> findByPhone(String phoneNumber);
    
    Optional<Customer> findByUserId(String userId);

    @Query("SELECT c FROM Customer c WHERE c.email = :value OR c.phone = :value")
    Optional<Customer> findByEmailOrPhone(@Param("value") String value);

    Page<Customer> findAllByReferenceIn(Set<String> customerRefs, Pageable pageable);
    List<Customer> findAllByReferenceIn(Set<String> customerRefs);
    Optional<Customer> findByReference(String reference);

    Page<Customer> findAllByRelatedBusinessIdsContaining(String businessId, Pageable pageable);
}