package ir.maktab127.repository;

import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.user.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ir.maktab127.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByStatus(OrderStatus status);
    @Query("SELECT o FROM Order o " +
            "WHERE o.service IN (SELECT sc FROM Specialist s JOIN s.serviceCategories sc WHERE s.id = :specialistId) " +
            "AND o.status = ir.maktab127.entity.OrderStatus.WAITING_FOR_PROPOSAL " +
            "OR o.status = ir.maktab127.entity.OrderStatus.WAITING_FOR_SPECIALIST_SELECTION")
    Page<Order> getAvailableOrdersForSpecialist(@Param("specialistId") Long specialistId, Pageable pageable);



    @Query("SELECT o FROM Order o WHERE o.service.id = :serviceCategoryId")
    List<Order> findByServiceCategoryId(@Param("serviceCategoryId") Long serviceCategoryId);
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o " +
            "WHERE o.service.id = :serviceCategoryId " +
            "AND o.status IN ('WAITING_FOR_PROPOSAL', 'WAITING_FOR_SPECIALIST_SELECTION', 'WAITING_FOR_SPECIALIST_ARRIVAL', 'IN_PROGRESS')")
    Boolean hasActualOrder(@Param("serviceCategoryId") Long serviceCategoryId);


    Page<Order> findByCustomer(Customer customer,
                               Pageable pageable);
    Page<Order> findByCustomerAndStatus(Customer customer, OrderStatus status,
                                        Pageable pageable);


}