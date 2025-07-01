package ir.maktab127.repository;

import ir.maktab127.entity.user.Customer;

import java.util.List;
import java.util.Optional;
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
    void delete(Customer customer);
}
