package ir.maktab127.service;

import ir.maktab127.entity.Payment;
import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.WalletTransaction;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.User;
import ir.maktab127.repository.PaymentRepository;
import ir.maktab127.repository.WalletRepository;
import ir.maktab127.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CustomerService customerRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;


    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return walletRepository.findById(id);
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    @Override
    public List<Wallet> getAll() {
        return walletRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        walletRepository.findById(id).ifPresent(walletRepository::delete);
    }

    @Override
    public void depositToSpecialist(Long specialistId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(specialistId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Override
    public void withdrawFromCustomer(Long customerId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        if (wallet.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient balance");
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    //phase 3
    @Override
    public BigDecimal getBalanceByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElse(java.math.BigDecimal.ZERO);
    }

    @Override
    public List<WalletTransaction> getTransactionsByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(wallet -> walletTransactionRepository.findByWalletId(wallet.getId()))
                .orElse(java.util.Collections.emptyList());
    }

    @Transactional
@Override
    public String createPaymentRequest(Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Payment paymentRequest = new Payment();
        paymentRequest.setToken(UUID.randomUUID().toString());
        paymentRequest.setUser(customer);
        paymentRequest.setAmount(amount);
        paymentRequest.setCreateDate(LocalDateTime.now());
        paymentRequest.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // 10 دقیقه مهلت
        paymentRequest.setUsed(false);
        paymentService.save(paymentRequest);

        paymentRepository.save(paymentRequest);
        return "http://localhost:8080/pay/" + paymentRequest.getToken();
    }

    @Override
    public boolean isValidPaymentToken(String token) {
        Payment paymentRequest = paymentRepository.findByToken(token)
                .orElse(null);
        return paymentRequest != null && !paymentRequest.isUsed() &&
                paymentRequest.getExpiresAt().isAfter(LocalDateTime.now());
    }
    @Override
    @Transactional
    public boolean processPayment(String token) {
        Payment paymentRequest = paymentRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (paymentRequest.isUsed() || paymentRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        Customer customer = customerRepository.findById(paymentRequest.getUser().getId()  ).orElseThrow();


        Wallet wallet = walletRepository.findByUser(customer)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(customer);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });

        wallet.setBalance(wallet.getBalance().add(paymentRequest.getAmount()));
        walletRepository.save(wallet);
        paymentRequest.setUsed(true);
        paymentRepository.save(paymentRequest);

        return true;
    }
}