package ir.maktab127.repository;

import ir.maktab127.entity.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(Long id);
    Optional<Wallet> findByUserId(Long userId);
    List<Wallet> findAll();
    void delete(Wallet wallet);
}