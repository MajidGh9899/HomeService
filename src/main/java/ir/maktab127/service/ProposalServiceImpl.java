package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
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
public class ProposalServiceImpl implements ProposalService {
    @Autowired
    private final ProposalRepository proposalRepository;
    @Autowired
    private final SpecialistRepository  specialistRepository;
    @Autowired
    private final OrderRepository orderRepository;



    @Override
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

        Proposal proposal = new Proposal();
        proposal.setSpecialist(specialist);
        proposal.setOrder(order);
        proposal.setProposedPrice(dto.getProposedPrice());
        proposal.setProposedStartTime(LocalDateTime.parse(dto.getStartDate()  + "T00:00:00"));
        proposal.setEndDate(LocalDateTime.parse(dto.getEndDate() + "T00:00:00"));
        proposal.setCreateDate(LocalDateTime.now());
        return proposalRepository.save(proposal);
    }

    //
    @Override
    public List<Proposal> getProposalsByOrder(Long orderId) {
        return proposalRepository.findByOrderId(orderId);
    }

    @Override
    public List<Proposal> getProposalsBySpecialist(Long specialistId) {
        return proposalRepository.findBySpecialistId(specialistId);
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
}
