package ir.maktab127.repository;

import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    Page<Proposal> findBySpecialistId(Long specialistId,Pageable pageable);
    Page<Proposal> findByOrderId(Long orderId,Pageable pageable);
    List<Proposal>findByOrderId(Long orderId);
    List<Proposal> findBySpecialistIdAndOrderId(Long specialistId, Long orderId);
    List<Proposal> findByStatus(ProposalStatus status);

    Optional<Proposal> findByOrderIdAndStatus(Long orderId,  ProposalStatus status);
}