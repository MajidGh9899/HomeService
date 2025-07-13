package ir.maktab127.repository;

import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findBySpecialistId(Long specialistId);
    List<Proposal> findByOrderId(Long orderId);
    List<Proposal> findBySpecialistIdAndOrderId(Long specialistId, Long orderId);
    List<Proposal> findByStatus(ProposalStatus status);
}