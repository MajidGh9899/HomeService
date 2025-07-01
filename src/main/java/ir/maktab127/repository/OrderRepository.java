package ir.maktab127.repository;

import ir.maktab127.entity.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    void delete(Order order);
}