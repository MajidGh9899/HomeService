package ir.maktab127.repository;

import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByUser(User user);
}