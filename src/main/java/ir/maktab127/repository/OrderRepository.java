package ir.maktab127.repository;

import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query("SELECT o FROM Order o WHERE " +
            "(:startDate IS NULL OR o.createDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.createDate <= :endDate) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:serviceCategoryId IS NULL OR o.service.id = :serviceCategoryId) AND " +
            "(:customerId IS NULL OR o.customer.id = :customerId) AND " +
            "(:specialistId IS NULL OR EXISTS (SELECT p FROM Proposal p WHERE p.order.id = o.id AND p.specialist.id = :specialistId AND p.status = 'ACCEPTED'))")
    List<Order> findOrdersWithFilters(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("status") OrderStatus status,
                                      @Param("serviceCategoryId") Long serviceCategoryId,
                                      @Param("customerId") Long customerId,
                                      @Param("specialistId") Long specialistId);
    List<Order> findByCustomer(Customer customer);
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);
}