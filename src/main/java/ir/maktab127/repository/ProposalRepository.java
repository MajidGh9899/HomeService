package ir.maktab127.repository;

import ir.maktab127.entity.Proposal;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository {
    Proposal save(Proposal proposal);
    Optional<Proposal> findById(Long id);
    List<Proposal> findAll();
    void delete(Proposal proposal);
}