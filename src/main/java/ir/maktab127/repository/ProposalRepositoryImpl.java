package ir.maktab127.repository;


import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
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

    //
    @Override
    public List<Proposal> findBySpecialistId(Long specialistId) {
        return entityManager.createQuery(
                        "SELECT p FROM Proposal p WHERE p.specialist.id = :specialistId", Proposal.class)
                .setParameter("specialistId", specialistId)
                .getResultList();
    }

    @Override
    public List<Proposal> findByOrderId(Long orderId) {
        return entityManager.createQuery(
                        "SELECT p FROM Proposal p WHERE p.order.id = :orderId", Proposal.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    @Override
    public List<Proposal> findBySpecialistIdAndOrderId(Long specialistId, Long orderId) {
        return entityManager.createQuery(
                        "SELECT p FROM Proposal p WHERE p.specialist.id = :specialistId AND p.order.id = :orderId",
                        Proposal.class)
                .setParameter("specialistId", specialistId)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    @Override
    public List<Proposal> findByStatus(ProposalStatus status) {
        return entityManager.createQuery(
                        "SELECT p FROM Proposal p WHERE p.status = :status", Proposal.class)
                .setParameter("status", status)
                .getResultList();
    }
}