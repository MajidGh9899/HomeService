package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ProposalRepository;

import ir.maktab127.repository.SpecialistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {
    @Autowired
    private final ProposalRepository proposalRepository;
    @Autowired
    private final SpecialistRepository  specialistRepository;
    @Autowired
    private final OrderRepository orderRepository;



    @Override
    @Transactional
    public Proposal save(Proposal proposal) {

        return proposalRepository.save(proposal);
    }

    @Override
    public Optional<Proposal> findById(Long id) {
         return proposalRepository.findById(id);
    }

    @Override
    public List<Proposal> getAll() {
         return proposalRepository.findAll();
    }

    @Override
    public void delete(Long id) {
             proposalRepository.findById(id).ifPresent(proposalRepository::delete);
    }
    @Transactional
    @Override
    public Proposal registerProposal(ProposalRegisterDto dto) {
        Specialist specialist = specialistRepository.findById(dto.getSpecialistId())
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // فقط اگر سفارش در گروه خدمات متخصص باشد
        boolean allowed = specialist.getServiceCategories().stream()
                .anyMatch(sc -> sc.getId().equals(order.getService().getId()));//query
        if (!allowed) throw new IllegalStateException("Specialist not allowed for this order's service");
        if(dto.getEndDate().isBefore(dto.getStartDate()) ||dto.getStartDate().isBefore(order.getStartDate())
        ||dto.getEndDate().isBefore(order.getStartDate())){
            throw new IllegalStateException("Invalid start and end date");
        }
        if(order.getStatus()== OrderStatus.WAITING_FOR_PROPOSAL){
            order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION);
            orderRepository.save(order);
        }

        Proposal proposal = new Proposal();
        proposal.setSpecialist(specialist);
        proposal.setOrder(order);
        proposal.setProposedPrice(dto.getProposedPrice());
        proposal.setProposedStartTime(dto.getStartDate() );
        proposal.setEndDate(dto.getEndDate() );
        proposal.setCreateDate(LocalDateTime.now());
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
    }

    //

    @Override
    public Page<Proposal> getProposalsByOrder(Long orderId, Pageable pageable) {
        return proposalRepository.findByOrderId(orderId,pageable);
    }

    @Override
    public List<Proposal> getProposalsByOrder(Long orderId) {
        return  proposalRepository.findByOrderId(orderId);
    }

    @Override
    public Page<Proposal> getProposalsBySpecialist(Long specialistId, Pageable pageable) {
        return proposalRepository.findBySpecialistId(specialistId,pageable);
    }

    @Override
    public void updateProposalStatus(Long proposalId, ProposalStatus status) {
        Optional<Proposal> proposalOpt = proposalRepository.findById(proposalId);
        if (proposalOpt.isPresent()) {
            Proposal proposal = proposalOpt.get();
            proposal.setStatus(status);
            proposalRepository.save(proposal);
        }
    }

    @Override
    public boolean isFirstProposalForOrder(Long orderId) {
        List<Proposal> proposals = proposalRepository.findByOrderId(orderId);
        return proposals.size() == 1;
    }

    @Override
    public Proposal getProposalByOrderAndSpecialist(Long orderId, Long specialistId) {
         return proposalRepository.findBySpecialistIdAndOrderId(specialistId, orderId).stream().findFirst().orElseThrow(() -> new RuntimeException("Proposal not found"));
    }
}
