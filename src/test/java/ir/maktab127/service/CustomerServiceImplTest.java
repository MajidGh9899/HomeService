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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerUpdateDto updateDto;
    private Wallet wallet;
    private WalletTransaction walletTransaction;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setPassword("password");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCreateDate(LocalDateTime.now());
        customer.setEmailVerified(false);
        customer.setEmailVerificationToken(UUID.randomUUID().toString());
        customer.setRoles(Set.of(Role.CUSTOMER));

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(customer);
        wallet.setBalance(BigDecimal.ZERO);

        walletTransaction = new WalletTransaction();
        walletTransaction.setId(1L);
        walletTransaction.setWallet(wallet);
        walletTransaction.setAmount(BigDecimal.ZERO);
        walletTransaction.setDescription("initial balance");

        updateDto = new CustomerUpdateDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setPassword("newPassword");
    }

    @Test
    void save_ValidCustomer_ReturnsSavedCustomer() {
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.save(customer);

        assertNotNull(result);
        assertEquals(customer, result);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findById_Exists_ReturnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_Exists_ReturnsCustomer() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findByEmail("customer@test.com");

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
    }

    @Test
    void findByEmail_NotExists_ReturnsEmpty() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.findByEmail("customer@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsAllCustomers() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> result = customerService.getAll();

        assertEquals(1, result.size());
        assertEquals(customer, result.get(0));
    }

    @Test
    void delete_CustomerExists_DeletesSuccessfully() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.delete(1L);

        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void delete_CustomerNotExists_NoAction() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        customerService.delete(1L);

        verify(customerRepository, never()).delete((Customer) any());
    }

    @Test
    void login_ValidCredentials_ReturnsCustomer() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.login("customer@test.com", "password");

        assertTrue(result.isPresent());
        assertEquals(customer, result.get());
    }

    @Test
    void login_InvalidCredentials_ReturnsEmpty() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.login("customer@test.com", "wrongPassword");

        assertFalse(result.isPresent());
    }

    @Test
    void login_EmailNotFound_ReturnsEmpty() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.login("customer@test.com", "password");

        assertFalse(result.isPresent());
    }

    @Test
    void updateInfo_ValidData_Success() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        customerService.updateInfo("customer@test.com", updateDto);

        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("newPassword", customer.getPassword());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void updateInfo_CustomerNotFound_ThrowsException() {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.updateInfo("customer@test.com", updateDto));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void register_NewCustomer_Success() throws MessagingException {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(walletTransaction);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        Customer result = customerService.register(customer);

        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Set.of(Role.CUSTOMER), result.getRoles());
        assertFalse(result.isEmailVerified());
        assertNotNull(result.getEmailVerificationToken());
        assertNotNull(result.getWallet());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(walletTransactionRepository, times(1)).save(any(WalletTransaction.class));
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() throws MessagingException {
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.register(customer));

        assertEquals("Email already exists", exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void verifyEmail_ValidToken_Success() {
        when(customerRepository.findByEmailVerificationToken(customer.getEmailVerificationToken())).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        customerService.verifyEmail(customer.getEmailVerificationToken());

        assertTrue(customer.isEmailVerified());
        assertNull(customer.getEmailVerificationToken());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsException() {
        when(customerRepository.findByEmailVerificationToken("invalid-token")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.verifyEmail("invalid-token"));

        assertEquals("Invalid verification token", exception.getMessage());
    }

    @Test
    void verifyEmail_AlreadyVerified_ThrowsException() {
        customer.setEmailVerified(true);
        when(customerRepository.findByEmailVerificationToken(customer.getEmailVerificationToken())).thenReturn(Optional.of(customer));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> customerService.verifyEmail(customer.getEmailVerificationToken()));

        assertEquals("Email already verified", exception.getMessage());
        verify(customerRepository, never()).save(any());
    }
}