package ir.maktab127.repository;

import ir.maktab127.entity.Wallet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class WalletRepositoryImpl implements WalletRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Wallet save(Wallet wallet) {
        if (wallet.getId() == null) {
            entityManager.persist(wallet);
            return wallet;
        } else {
            return entityManager.merge(wallet);
        }
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Wallet.class, id));
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        List<Wallet> result = entityManager.createQuery("SELECT w FROM Wallet w WHERE w.user.id = :userId", Wallet.class)
                .setParameter("userId", userId)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Wallet> findAll() {
        return entityManager.createQuery("SELECT w FROM Wallet w", Wallet.class).getResultList();
    }

    @Override
    public void delete(Wallet wallet) {
        entityManager.remove(entityManager.contains(wallet) ? wallet : entityManager.merge(wallet));
    }
}

