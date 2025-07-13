package ir.maktab127.repository;

import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.service.id = :serviceCategoryId")
    List<Order> findByServiceCategoryId(@Param("serviceCategoryId") Long serviceCategoryId);
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o " +
            "WHERE o.service.id = :serviceCategoryId " +
            "AND o.status IN ('WAITING_FOR_PROPOSAL', 'WAITING_FOR_SPECIALIST_SELECTION', 'WAITING_FOR_SPECIALIST_ARRIVAL', 'IN_PROGRESS')")
    Boolean hasActualOrder(@Param("serviceCategoryId") Long serviceCategoryId);
}