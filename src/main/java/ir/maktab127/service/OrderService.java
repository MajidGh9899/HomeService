package ir.maktab127.service;

import java.util.List;
import java.util.Optional;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;

public interface OrderService {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> getAll();
    void delete(Long id);

    void completedOrder(Long orderId);

    void payToSpecialist(Long orderId, Long specialistId);
    Order registerOrder(OrderRegisterDto dto);

    //
    void updateOrderStatus(Long orderId, OrderStatus status);
    List<Order> getOrdersByStatus(OrderStatus status);
    List<Order> getOrdersByServiceCategory(Long serviceCategoryId);

    void payOrder(Long orderId, PaymentRequestDto paymentRequest);
}