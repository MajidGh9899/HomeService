package ir.maktab127.repository;


import ir.maktab127.entity.Proposal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public class ProposalRepositoryImpl implements ProposalRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Proposal save(Proposal proposal) {
        if (proposal.getId() == null) {
            entityManager.persist(proposal);
            return proposal;
        } else {
            return entityManager.merge(proposal);
        }
    }

    @Override
    public Optional<Proposal> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Proposal.class, id));
    }

    @Override
    public List<Proposal> findAll() {
        return entityManager.createQuery("SELECT p FROM Proposal p", Proposal.class).getResultList();
    }

    @Override
    public void delete(Proposal proposal) {
        entityManager.remove(entityManager.contains(proposal) ? proposal : entityManager.merge(proposal));
    }
}