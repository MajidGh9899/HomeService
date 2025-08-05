package ir.maktab127.service;

import java.util.List;
import java.util.Optional;

import ir.maktab127.dto.CustomerUpdateDto;
import ir.maktab127.entity.user.Customer;
import jakarta.mail.MessagingException;

public interface CustomerService {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByEmail(String email);
    List<Customer> getAll();
    void delete(Long id);
    Optional<Customer> login(String email, String password);
    void updateInfo(String email, CustomerUpdateDto dto);

    void verifyEmail(String token) throws IllegalStateException;
    Customer register(Customer customer) throws MessagingException;
}