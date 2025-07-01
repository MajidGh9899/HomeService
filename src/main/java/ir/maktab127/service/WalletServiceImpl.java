package ir.maktab127.service;

import ir.maktab127.entity.Wallet;
import ir.maktab127.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
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
}