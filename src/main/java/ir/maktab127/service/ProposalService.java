package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;

import java.util.List;
import java.util.Optional;

public interface ProposalService {
    Proposal save(Proposal proposal);
    Optional<Proposal> findById(Long id);
    List<Proposal> getAll();
    void delete(Long id);
    Proposal registerProposal(ProposalRegisterDto dto);

    //
    List<Proposal> getProposalsByOrder(Long orderId);
    List<Proposal> getProposalsBySpecialist(Long specialistId);
    void updateProposalStatus(Long proposalId, ProposalStatus status);
    boolean isFirstProposalForOrder(Long orderId);
}