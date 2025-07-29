package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.Wallet;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Role;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ProposalRepository;
import ir.maktab127.repository.SpecialistRepository;
import ir.maktab127.repository.WalletRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpecialistServiceImpl implements SpecialistService {
    @Autowired
    private final SpecialistRepository specialistRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProposalRepository proposalRepository;
    @Autowired
    private final WalletRepository walletRepository;
    private final   EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    @Override
    public Specialist register(Specialist specialist) throws MessagingException {
        // تنظیم وضعیت حساب کاربری
        if (specialistRepository.findByEmail(specialist.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        specialist.setStatus(AccountStatus.NEW);

        specialist.setPassword(passwordEncoder.encode(specialist.getPassword()));
        specialist.setRoles(Set.of(Role.SPECIALIST));
        specialist.setEmailVerified(false);
        specialist.setEmailVerificationToken(UUID.randomUUID().toString());
        Specialist savedSpecialist = specialistRepository.save(specialist);

        // Send verification email
        emailService.sendVerificationEmail(savedSpecialist.getEmail(), savedSpecialist.getEmailVerificationToken());

        return savedSpecialist;
    }
    @Transactional
    public void verifyEmail(String token) {
        Specialist specialist = specialistRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
        if (specialist.isEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }
        specialist.setEmailVerified(true);
        specialist.setEmailVerificationToken(null); // Invalidate token
        if(specialist.getProfileImage()!=null)
            updateSpecialistStatus(specialist);
        specialistRepository.save(specialist);
    }
    @Transactional
    public Specialist updateProfileImage(Long id, String base64Image) {
        Specialist specialist = specialistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));
        specialist.setProfileImage(base64Image);
        updateSpecialistStatus(specialist);
        return specialistRepository.save(specialist);
    }

    private void updateSpecialistStatus(Specialist specialist) {
        if (specialist.isEmailVerified() && specialist.getProfileImage() != null) {
            specialist.setStatus(AccountStatus.PENDING);
        }else {
            throw new RuntimeException("Email not verified or profile image not uploaded");
        }
    }

    @Override
    public Optional<Specialist> findById(Long id) {
        return specialistRepository.findById(id);
    }

    @Override
    public Optional<Specialist> findByEmail(String email) {
        return specialistRepository.findByEmail(email);
    }

    @Override
    public List<Specialist> getAll() {
        return specialistRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        specialistRepository.findById(id).ifPresent(specialistRepository::delete);
    }
    @Override
    public Optional<Specialist> login(String email, String password) {
        return specialistRepository.findByEmail(email)
                .filter(s -> s.getPassword().equals(password) && s.getStatus() == AccountStatus.APPROVED);
    }
    @Transactional
    @Override
    public void updateInfo(Long specialistId, SpecialistUpdateDto dto) {
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));

        // بررسی نداشتن کار فعال
        boolean hasActiveOrder = orderRepository.hasActualOrder(specialistId);

        if (hasActiveOrder) {
            throw new IllegalStateException("Specialist has active work and cannot update info now.");
        }

        specialist.setEmail(dto.getEmail());
        specialist.setPassword(dto.getPassword());
        specialist.setProfileImage(dto.getProfileImage());

        specialist.setStatus(AccountStatus.PENDING);
        specialistRepository.save(specialist);
    }

    @Override
    public Proposal submitProposal(Long specialistId, Long orderId, Proposal proposal) {

        if (!canSubmitProposal(specialistId, orderId)) {
            throw new IllegalStateException("Specialist cannot submit proposal for this order");
        }


        proposal.setCreateDate(LocalDateTime.now());


        return proposalRepository.save(proposal);
    }

    @Override
    public List<Order> getAvailableOrdersForSpecialist(Long specialistId) {

        return orderRepository.findByStatusIn(List.of(

                OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL
        ));
    }

    @Override
    public List<Proposal> getSpecialistProposals(Long specialistId) {
        return proposalRepository.findBySpecialistId(specialistId);
    }

    @Override
    public boolean canSubmitProposal(Long specialistId, Long orderId) {
        // check if specialist exists and is approved
        Optional<Specialist> specialistOpt = specialistRepository.findById(specialistId);
        if (specialistOpt.isEmpty() || specialistOpt.get().getStatus() != AccountStatus.APPROVED) {
            return false;
        }

        // check if order exists and is in correct status
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.WAITING_FOR_PROPOSAL &&
                order.getStatus() != OrderStatus.WAITING_FOR_SPECIALIST_SELECTION
                && order.getStatus() != OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL) {
            return false;
        }

        // check if specialist hasn't already submitted a proposal for this order
        List<Proposal> existingProposals = proposalRepository.findBySpecialistIdAndOrderId(specialistId, orderId);
        return existingProposals.isEmpty();
    }
}