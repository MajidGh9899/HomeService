package ir.maktab127.service;

import ir.maktab127.dto.CustomerUpdateDto;
import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.WalletTransaction;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Role;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.WalletRepository;
import ir.maktab127.repository.WalletTransactionRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

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
    public void updateInfo(String email, CustomerUpdateDto dto) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());

        customer.setPassword(dto.getPassword());
        customerRepository.save(customer);
    }
    @Transactional
    public Customer register(Customer customer) throws MessagingException {

        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }


        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setRoles(Set.of(Role.CUSTOMER));

        customer.setCreateDate(LocalDateTime.now());
        customer.setEmailVerified(false);
        customer.setEmailVerificationToken(UUID.randomUUID().toString());

        Customer savedCustomer = customerRepository.save(customer);

        // Send verification email
        emailService.sendVerificationEmail(savedCustomer.getEmail(), savedCustomer.getEmailVerificationToken());
        Wallet wallet = new Wallet();
        wallet.setUser(savedCustomer);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);
        savedCustomer.setWallet(wallet);
        WalletTransaction walletTransaction=new WalletTransaction();
        walletTransaction.setWallet(wallet);
        walletTransaction.setAmount(BigDecimal.ZERO);
        walletTransaction.setDescription("initial balance");
        walletTransactionRepository.save(walletTransaction);
        return savedCustomer;
    }
    @Override
    @Transactional
    public void verifyEmail(String token) {
        Customer customer = customerRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
        if (customer.isEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }
        customer.setEmailVerified(true);
        customer.setEmailVerificationToken(null);

        customerRepository.save(customer);
    }
}
