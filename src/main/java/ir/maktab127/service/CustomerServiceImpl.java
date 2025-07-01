package ir.maktab127.service;

import ir.maktab127.dto.CustomerUpdateDto;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Override
    public Customer save(Customer customer) { return customerRepository.save(customer); }
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
