package ir.maktab127.controller;

import ir.maktab127.dto.*;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.service.OrderService;
import ir.maktab127.service.ProposalService;
import ir.maktab127.service.SpecialistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/specialist")
@RequiredArgsConstructor
@Validated
public class SpecialistController {
    private final SpecialistService specialistService;
    private final ProposalService proposalService;
    private final OrderService orderService;



    @PostMapping("/register")
    public ResponseEntity<SpecialistResponseDto> register(@Valid @RequestBody SpecialistRegisterDto dto) {
        Specialist specialist = SpecialistMapper.toEntity(dto);
        Specialist saved = specialistService.register(specialist);
        return ResponseEntity.ok(SpecialistMapper.toResponseDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialistResponseDto> getById(@PathVariable Long id) {
        Optional<Specialist> specialist = specialistService.findById(id);
        return specialist.map(s -> ResponseEntity.ok(SpecialistMapper.toResponseDto(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<SpecialistResponseDto> getAll() {
        return specialistService.getAll().stream()
                .map(SpecialistMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specialistService.delete(id);
        return ResponseEntity.noContent().build();
    }
    //
    @PostMapping("/login")
    public ResponseEntity<SpecialistResponseDto> login(@Valid @RequestBody SpecialistLoginDto dto) {
        Optional<Specialist> specialist = specialistService.login(dto.getEmail(), dto.getPassword());
        return specialist
                .map(SpecialistMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
    //
    @PutMapping("/{id}/update-info")
    public ResponseEntity<Void> updateInfo(@PathVariable Long id, @Valid @RequestBody SpecialistUpdateDto dto) {
        try {
            specialistService.updateInfo(id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null); // Conflict
        }
    }
    //offer to do a service
    @PostMapping("/addProposal")
    public ResponseEntity<ProposalResponseDto> registerProposal(@Valid @RequestBody ProposalRegisterDto dto) {
        try {
            Proposal proposal = proposalService.registerProposal(dto);
            return ResponseEntity.ok(ProposalMapper.toResponseDto(proposal));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(409).build();
        }
    }
    //  completed
    //phase2
    // Get Available Orders for Specialist
    @GetMapping("/{specialistId}/available-orders")
    public ResponseEntity<List<Order>> getAvailableOrders(@PathVariable Long specialistId) {
        List<Order> availableOrders = specialistService.getAvailableOrdersForSpecialist(specialistId);
        return ResponseEntity.ok(availableOrders);
    }

    // Submit Proposal
    @PostMapping("/{specialistId}/proposals")
    public ResponseEntity<ProposalResponseDto> submitProposal(
            @PathVariable Long specialistId,
            @Valid @RequestBody ProposalRegisterDto dto) {


        if (!specialistService.canSubmitProposal(specialistId, dto.getOrderId())) {
            return ResponseEntity.badRequest().build();
        }

        // Create proposal entity
        Optional<Specialist> specialistOpt = specialistService.findById(specialistId);
        Optional<Order> orderOpt = orderService.findById(dto.getOrderId());

        if (specialistOpt.isEmpty() || orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Proposal proposal = ProposalMapper.toEntity(dto, orderOpt.get(), specialistOpt.get());
        Proposal savedProposal = proposalService.save(proposal);


        if (proposalService.isFirstProposalForOrder(dto.getOrderId())) {

            orderService.updateOrderStatus(dto.getOrderId(),
                    OrderStatus.WAITING_FOR_SPECIALIST_SELECTION);
        }

        return ResponseEntity.ok(ProposalMapper.toResponseDto(savedProposal));
    }

    // Get Specialist's Proposals
    @GetMapping("/{specialistId}/proposals")
    public ResponseEntity<List<ProposalResponseDto>> getSpecialistProposals(@PathVariable Long specialistId) {
        List<Proposal> proposals = specialistService.getSpecialistProposals(specialistId);
        List<ProposalResponseDto> responseDtos = proposals.stream()
                .map(ProposalMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    // Get Proposal by ID
    @GetMapping("/proposals/{proposalId}")
    public ResponseEntity<ProposalResponseDto> getProposalById(@PathVariable Long proposalId) {
        Optional<Proposal> proposal = proposalService.findById(proposalId);
        return proposal.map(p -> ResponseEntity.ok(ProposalMapper.toResponseDto(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update Proposal Status
    @PatchMapping("/proposals/{proposalId}/status")
    public ResponseEntity<Void> updateProposalStatus(
            @PathVariable Long proposalId,
            @RequestParam String status) {

        try {
            ProposalStatus proposalStatus =
                    ProposalStatus.valueOf(status.toUpperCase());
            proposalService.updateProposalStatus(proposalId, proposalStatus);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
