package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.Proposal;

import java.util.List;
import java.util.Optional;

public interface ProposalService {
    Proposal save(Proposal proposal);
    Optional<Proposal> findById(Long id);
    List<Proposal> getAll();
    void delete(Long id);
    Proposal registerProposal(ProposalRegisterDto dto);
}