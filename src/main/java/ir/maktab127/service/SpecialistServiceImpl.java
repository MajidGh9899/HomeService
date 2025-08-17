package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Role;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final WalletTransactionRepository walletTransactionRepository;


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
        Wallet wallet = new Wallet();
        wallet.setUser(specialist);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);
        WalletTransaction walletTransaction=new WalletTransaction();
        walletTransaction.setWallet(wallet);
        walletTransaction.setAmount(BigDecimal.ZERO);
        walletTransaction.setDescription("initial balance");
        walletTransactionRepository.save(walletTransaction);

        specialist.setWallet(wallet);
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
    public void updateProfileImage(Long id, String base64Image) {
        Specialist specialist = specialistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));
        specialist.setProfileImage(base64Image);
        updateSpecialistStatus(specialist);
        specialistRepository.save(specialist);
    }

    private void updateSpecialistStatus(Specialist specialist) {
        if (specialist.isEmailVerified() && specialist.getProfileImage() != null && !specialist.getProfileImage().isBlank()) {
            specialist.setStatus(AccountStatus.PENDING);
        }
        else {
            specialist.setStatus(AccountStatus.NEW);
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
    public Page<Specialist> getAll(Pageable page) {
        return specialistRepository.findAll(page);
    }

    @Override
    public void delete(Long id) {
        specialistRepository.findById(id).ifPresent(specialistRepository::delete);
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

        specialist.setPassword(dto.getPassword());
        specialist.setProfileImage(dto.getProfileImage());

        specialist.setStatus(AccountStatus.PENDING);
        specialistRepository.save(specialist);
    }
    @Transactional
    @Override
    public Proposal submitProposal(Long specialistId, Long orderId, Proposal proposal) {

        if (!canSubmitProposal(specialistId, orderId)) {
            throw new IllegalStateException("Specialist cannot submit proposal for this order");
        }


        proposal.setCreateDate(LocalDateTime.now());
        if(proposal.getOrder().getStatus()==OrderStatus.WAITING_FOR_PROPOSAL){
            proposal.getOrder().setStatus(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION);
            orderRepository.save(proposal.getOrder());
        }



        return proposalRepository.save(proposal);
    }

    @Override
    public Page<Order> getAvailableOrdersForSpecialist(Long specialistId, Pageable page) {
        Specialist specialistOpt = specialistRepository.findById(specialistId).orElseThrow();
        List<ServiceCategory> services=specialistOpt.getServiceCategories();

        return orderRepository.getAvailableOrdersForSpecialist(specialistId, page);
    }

    @Override
    public Page<Proposal> getSpecialistProposals(Long specialistId,Pageable page) {
        return proposalRepository.findBySpecialistId(specialistId,page);
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