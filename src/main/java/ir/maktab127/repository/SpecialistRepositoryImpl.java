package ir.maktab127.repository;

import ir.maktab127.entity.user.Specialist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository

public class SpecialistRepositoryImpl implements SpecialistRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Specialist save(Specialist specialist) {
        if (specialist.getId() == null) {
            entityManager.persist(specialist);
            return specialist;
        } else {
            return entityManager.merge(specialist);
        }
    }

    @Override
    public Optional<Specialist> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Specialist.class, id));
    }

    @Override
    public Optional<Specialist> findByEmail(String email) {
        List<Specialist> result = entityManager.createQuery(
                        "SELECT s FROM Specialist s WHERE s.email = :email", Specialist.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Specialist> findAll() {
        return entityManager.createQuery("SELECT s FROM Specialist s", Specialist.class)
                .getResultList();
    }

    @Override
    public void delete(Specialist specialist) {
        entityManager.remove(entityManager.contains(specialist) ? specialist : entityManager.merge(specialist));
    }
}