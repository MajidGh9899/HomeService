package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.user.Specialist;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SpecialistService {
    Specialist register(Specialist specialist) throws IOException, MessagingException;
    Optional<Specialist> findById(Long id);
    Optional<Specialist> findByEmail(String email);
    Page<Specialist> getAll(Pageable page);
    void delete(Long id);

    void updateInfo(Long specialistId, SpecialistUpdateDto dto) throws IllegalStateException;

    //Phase2-for Proposal
    Proposal submitProposal(Long specialistId, Long orderId, Proposal proposal);
    Page<Order> getAvailableOrdersForSpecialist(Long specialistId, Pageable page);
    Page<Proposal> getSpecialistProposals(Long specialistId,Pageable page);
    boolean canSubmitProposal(Long specialistId, Long orderId);

    void verifyEmail(String token) throws IllegalStateException;
    void updateProfileImage(Long id, String base64Image);


}