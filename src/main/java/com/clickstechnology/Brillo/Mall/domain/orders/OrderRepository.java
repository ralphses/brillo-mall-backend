package com.clickstechnology.Brillo.Mall.domain.orders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByReference(String reference);

    @Query("""
       SELECT o
       FROM Order o
       LEFT JOIN FETCH o.items oi
       WHERE o.orderId = :orderId
       """)
    Optional<Order> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT o FROM Order o " +
            "WHERE o.orderId = :orderId " +
            "AND o.customerId = :customerId")
    Optional<Order> findByOrderIdAndCustomerId(
            @Param("orderId") String orderId,
            @Param("customerId") String customerId);

    Page<Order> findAllByCustomerId(
            @Param("customerId") String customerId,
            Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "WHERE o.orderId = :orderId " +
            "AND o.customerId = :customerId")
    Optional<Order> findOrderDetailsByOrderIdAndCustomerId(
            @Param("orderId") String orderId,
            @Param("customerId") String customerId);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "WHERE o.orderId = :orderId AND o.businessId IN :businessIds")
    Optional<Order> findOrderForBusinessAdmin(
            @Param("orderId") String orderId,
            @Param("businessIds") List<String> businessIds);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items oi " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findAnyByOrderId(@Param("orderId") String orderId);

    Page<Order> findAllByBusinessId(
            @Param("businessId") String businessId,
            Pageable pageable);

    Page<Order> findAllByBusinessIdIn(
            @Param("businessIds") List<String> businessIds,
            Pageable pageable);

    Page<Order> findAllByUserId(
            @Param("userId") String userId,
            Pageable pageable);
}
