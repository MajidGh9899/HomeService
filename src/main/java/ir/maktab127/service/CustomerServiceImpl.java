package ir.maktab127.service;

import ir.maktab127.dto.CustomerUpdateDto;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer save(Customer customer) {


        return customerRepository.save(customer); }
    @Override
    public Optional<Customer> findById(Long id) { return customerRepository.findById(id); }
    @Override
    public Optional<Customer> findByEmail(String email) { return customerRepository.findByEmail(email); }
    @Override
    public List<Customer> getAll() { return customerRepository.findAll(); }
    @Override
    public void delete(Long id) { customerRepository.findById(id).ifPresent(customerRepository::delete); }
    @Override
    public Optional<Customer> login(String email, String password) {
        return customerRepository.findByEmail(email)
                .filter(c -> c.getPassword().equals(password));
    }
    @Override
    @Transactional
    public void updateInfo(Long customerId, CustomerUpdateDto dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(dto.getPassword());
        customerRepository.save(customer);
    }
}
