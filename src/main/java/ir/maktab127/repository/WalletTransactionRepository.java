package ir.maktab127.repository;

import ir.maktab127.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository  extends JpaRepository<WalletTransaction,Long> {
    Page<WalletTransaction> findByWalletId(Long walletId, Pageable pageable);
}
