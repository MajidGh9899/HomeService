package ir.maktab127.service;

import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.WalletTransaction;
import ir.maktab127.repository.WalletRepository;
import ir.maktab127.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public Wallet save(Wallet wallet) { return walletRepository.save(wallet); }
    @Override
    public Optional<Wallet> findById(Long id) { return walletRepository.findById(id); }
    @Override
    public Optional<Wallet> findByUserId(Long userId) { return walletRepository.findByUserId(userId); }
    @Override
    public List<Wallet> getAll() { return walletRepository.findAll(); }
    @Override
    public void delete(Long id) { walletRepository.findById(id).ifPresent(walletRepository::delete); }
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
    public java.math.BigDecimal getBalanceByUserId(Long userId) {
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
}