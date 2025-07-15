package ir.maktab127.repository;

import ir.maktab127.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository  extends JpaRepository<WalletTransaction,Long> {
    List<WalletTransaction> findByWalletId(Long walletId);
}
