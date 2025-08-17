package ir.maktab127.service;

import ir.maktab127.entity.Payment;
import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.WalletTransaction;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.PaymentRepository;
import ir.maktab127.repository.WalletRepository;
import ir.maktab127.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet wallet;
    private Customer customer;
    private Payment payment;
    private WalletTransaction transaction;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(customer);
        wallet.setBalance(new BigDecimal("100.00"));

        payment = new Payment();
        payment.setId(1L);
        payment.setUser(customer);
        payment.setAmount(new BigDecimal("50.00"));
        payment.setToken(UUID.randomUUID().toString());
        payment.setCreateDate(LocalDateTime.now());
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        payment.setUsed(false);

        transaction = new WalletTransaction();
        transaction.setId(1L);
        transaction.setWallet(wallet);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setDescription("Test transaction");
    }

    @Test
    void save_ValidWallet_ReturnsSavedWallet() {
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet result = walletService.save(wallet);

        assertNotNull(result);
        assertEquals(wallet, result);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void findById_Exists_ReturnsWallet() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        Optional<Wallet> result = walletService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(wallet, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Wallet> result = walletService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByUserId_Exists_ReturnsWallet() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Optional<Wallet> result = walletService.findByUserId(1L);

        assertTrue(result.isPresent());
        assertEquals(wallet, result.get());
    }

    @Test
    void findByUserId_NotExists_ReturnsEmpty() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        Optional<Wallet> result = walletService.findByUserId(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsAllWallets() {
        List<Wallet> wallets = Arrays.asList(wallet);
        when(walletRepository.findAll()).thenReturn(wallets);

        List<Wallet> result = walletService.getAll();

        assertEquals(1, result.size());
        assertEquals(wallet, result.get(0));
    }

    @Test
    void delete_WalletExists_DeletesSuccessfully() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        walletService.delete(1L);

        verify(walletRepository, times(1)).delete(wallet);
    }

    @Test
    void delete_WalletNotExists_NoAction() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        walletService.delete(1L);

        verify(walletRepository, never()).delete(any());
    }

    @Test
    void depositToSpecialist_ValidData_Success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.depositToSpecialist(1L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void depositToSpecialist_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.depositToSpecialist(1L, new BigDecimal("50.00")));

        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void withdrawFromCustomer_ValidData_Success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        walletService.withdrawFromCustomer(1L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void withdrawFromCustomer_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.withdrawFromCustomer(1L, new BigDecimal("50.00")));

        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void withdrawFromCustomer_InsufficientBalance_ThrowsException() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> walletService.withdrawFromCustomer(1L, new BigDecimal("150.00")));

        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void getBalanceByUserId_WalletExists_ReturnsBalance() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        BigDecimal result = walletService.getBalanceByUserId(1L);

        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void getBalanceByUserId_WalletNotExists_ReturnsZero() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        BigDecimal result = walletService.getBalanceByUserId(1L);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getTransactionsByUserId_WalletExists_ReturnsTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WalletTransaction> page = new PageImpl<>(Arrays.asList(transaction));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByWalletId(1L, pageable)).thenReturn(page);

        Page<WalletTransaction> result = walletService.getTransactionsByUserId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(transaction, result.getContent().get(0));
    }

    @Test
    void getTransactionsByUserId_WalletNotExists_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> walletService.getTransactionsByUserId(1L, pageable));
    }





    @Test
    void isValidPaymentToken_ValidToken_ReturnsTrue() {
        when(paymentRepository.findByToken(payment.getToken())).thenReturn(Optional.of(payment));

        boolean result = walletService.isValidPaymentToken(payment.getToken());

        assertTrue(result);
    }

    @Test
    void isValidPaymentToken_InvalidToken_ReturnsFalse() {
        when(paymentRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        boolean result = walletService.isValidPaymentToken("invalid-token");

        assertFalse(result);
    }

    @Test
    void isValidPaymentToken_UsedToken_ReturnsFalse() {
        payment.setUsed(true);
        when(paymentRepository.findByToken(payment.getToken())).thenReturn(Optional.of(payment));

        boolean result = walletService.isValidPaymentToken(payment.getToken());

        assertFalse(result);
    }

    @Test
    void isValidPaymentToken_ExpiredToken_ReturnsFalse() {
        payment.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(paymentRepository.findByToken(payment.getToken())).thenReturn(Optional.of(payment));

        boolean result = walletService.isValidPaymentToken(payment.getToken());

        assertFalse(result);
    }



    @Test
    void processPayment_InvalidToken_ThrowsException() {
        when(paymentRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.processPayment("invalid-token"));

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void processPayment_UsedToken_ReturnsFalse() {
        payment.setUsed(true);
        when(paymentRepository.findByToken(payment.getToken())).thenReturn(Optional.of(payment));

        boolean result = walletService.processPayment(payment.getToken());

        assertFalse(result);
    }

    @Test
    void processPayment_ExpiredToken_ReturnsFalse() {
        payment.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(paymentRepository.findByToken(payment.getToken())).thenReturn(Optional.of(payment));

        boolean result = walletService.processPayment(payment.getToken());

        assertFalse(result);
    }



}