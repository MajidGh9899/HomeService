package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ProposalRepository;
import ir.maktab127.repository.SpecialistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class SpecialistServiceImpl implements SpecialistService {
    @Autowired
    private final SpecialistRepository specialistRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProposalRepository proposalRepository;



    @Transactional
    @Override
    public Specialist register(Specialist specialist) {
        specialist.setStatus(AccountStatus.NEW);
        specialist.setRegisterDate(LocalDateTime.now());
        return specialistRepository.save(specialist);
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
        specialist.setProfileImagePath(dto.getProfileImagePath());
        specialist.setStatus(AccountStatus.PENDING);
        specialistRepository.save(specialist);
    }

    @Override
    public Proposal submitProposal(Long specialistId, Long orderId, Proposal proposal) {

        if (!canSubmitProposal(specialistId, orderId)) {
            throw new IllegalStateException("Specialist cannot submit proposal for this order");
        }


        proposal.setCreateDate(ZonedDateTime.from(LocalDateTime.now()));


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