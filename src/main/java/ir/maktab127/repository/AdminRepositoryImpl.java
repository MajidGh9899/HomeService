package ir.maktab127.repository;

import ir.maktab127.entity.user.Admin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public class AdminRepositoryImpl implements AdminRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Admin save(Admin admin) {
        if (admin.getId() == null) {
            entityManager.persist(admin);
            return admin;
        } else {
            return entityManager.merge(admin);
        }
    }

    @Override
    public Optional<Admin> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Admin.class, id));
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        List<Admin> result = entityManager.createQuery("SELECT a FROM Admin a WHERE a.email = :email", Admin.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Admin> findAll() {
        return entityManager.createQuery("SELECT a FROM Admin a", Admin.class).getResultList();
    }

    @Override
    public void delete(Admin admin) {
        entityManager.remove(entityManager.contains(admin) ? admin : entityManager.merge(admin));
    }
}
