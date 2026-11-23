package com.fyndhacks.console.repository;
import com.fyndhacks.console.entity.Order;
import com.fyndhacks.console.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id=:id")
    Optional<Order> findByIdLocked(@Param("id") Long id);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByOrderReferenceContainingIgnoreCaseOrderByCreatedAtDesc(String reference);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findByOrderReferenceContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(String reference, OrderStatus status);
}
