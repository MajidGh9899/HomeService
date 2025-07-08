package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ProposalRepository;
import ir.maktab127.repository.ProposalRepositoryImpl;
import ir.maktab127.repository.SpecialistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class ProposalServiceImpl implements ProposalService {
    private final ProposalRepository proposalRepository;
    private final SpecialistRepository  specialistRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public ProposalServiceImpl(ProposalRepository proposalRepository, SpecialistRepository specialistRepository, OrderRepository orderRepository) {
        this.proposalRepository = proposalRepository;
        this.specialistRepository = specialistRepository;
        this.orderRepository = orderRepository;
    }

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
        proposal.setCreatedAt(LocalDateTime.now());
        return proposalRepository.save(proposal);
    }
}
