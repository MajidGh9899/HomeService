package ir.maktab127.service;

import java.util.List;
import java.util.Optional;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.entity.Order;

public interface OrderService {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> getAll();
    void delete(Long id);

    void completedOrder(Long orderId);

    void payToSpecialist(Long orderId, Long specialistId);
    Order registerOrder(OrderRegisterDto dto);
}