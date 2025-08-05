package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProposalService {
    Proposal save(Proposal proposal);
    Optional<Proposal> findById(Long id);
    List<Proposal> getAll();
    void delete(Long id);
    Proposal registerProposal(ProposalRegisterDto dto);

    //
    Page<Proposal> getProposalsByOrder(Long orderId, Pageable page);
    List<Proposal>getProposalsByOrder(Long orderId);
    Page<Proposal> getProposalsBySpecialist(Long specialistId, Pageable page);
    void updateProposalStatus(Long proposalId, ProposalStatus status);
    boolean isFirstProposalForOrder(Long orderId);

    //phase 3
    Proposal getProposalByOrderAndSpecialist(Long orderId, Long specialistId);
}