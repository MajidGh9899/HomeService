package ir.maktab127.service;

import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletService {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(Long id);
    Optional<Wallet> findByUserId(Long userId);
    List<Wallet> getAll();
    void delete(Long id);
    void depositToSpecialist(Long specialistId, BigDecimal amount);
    void withdrawFromCustomer(Long customerId, BigDecimal amount);

    //phase 3
    BigDecimal getBalanceByUserId(Long userId);

    List<WalletTransaction> getTransactionsByUserId(Long userId);
    boolean processPayment(String token);
    String createPaymentRequest(Long customerId, BigDecimal amount);
    boolean isValidPaymentToken(String token);
}