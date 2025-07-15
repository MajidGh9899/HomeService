package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.user.Specialist;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SpecialistService {
    Specialist register(Specialist specialist, MultipartFile profileImage) throws IOException;
    Optional<Specialist> findById(Long id);
    Optional<Specialist> findByEmail(String email);
    List<Specialist> getAll();
    void delete(Long id);
    Optional<Specialist> login(String email, String password);
    void updateInfo(Long specialistId, SpecialistUpdateDto dto) throws IllegalStateException;

    //Phase2-for Proposal
    Proposal submitProposal(Long specialistId, Long orderId, Proposal proposal);
    List<Order> getAvailableOrdersForSpecialist(Long specialistId);
    List<Proposal> getSpecialistProposals(Long specialistId);
    boolean canSubmitProposal(Long specialistId, Long orderId);


}