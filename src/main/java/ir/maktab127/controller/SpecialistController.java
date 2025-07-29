package ir.maktab127.controller;

import ir.maktab127.dto.*;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
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
    private final CommentService commentService;
    private final WalletService walletService;





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
    @PostMapping(value = "/update-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(
            @RequestParam Long specialistId,
            @RequestPart("profileImage") MultipartFile profileImage) throws IOException {
        // اعتبارسنجی و ذخیره تصویر

        if (profileImage != null && !profileImage.isEmpty()) {
            String base64Image = Base64.getEncoder().encodeToString(profileImage.getBytes());


            specialistService.updateProfileImage(specialistId, base64Image);
        }
        return ResponseEntity.ok("Profile image updated");
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
    @PostMapping("/proposals-submit/{specialistId}")
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
    // phse 3 history
    @GetMapping("/{specialistId}/orders/history")
    public ResponseEntity<List<OrderResponseDto>> getOrderHistory(@PathVariable Long specialistId) {
        List<OrderResponseDto> history = proposalService.getProposalsBySpecialist(specialistId)
                .stream()
                .map(OrderMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    //comment and averge rating
    @GetMapping("/{specialistId}/rating/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long specialistId) {
        Double avg = commentService.getAverageRatingForSpecialist(specialistId);
        return ResponseEntity.ok(avg);
    }

    // مشاهده امتیاز سفارش خاص
    @GetMapping("/orders/rating/{specialistId}/{orderId}")
    public ResponseEntity<Integer> getOrderRating(@PathVariable Long specialistId, @PathVariable Long orderId) {
        Integer rating = commentService.getOrderRatingForSpecialist(specialistId, orderId);
        return ResponseEntity.ok(rating);
    }

    //کیف پول
    @GetMapping("/{specialistId}/balance")
    public ResponseEntity<java.math.BigDecimal> getBalance(@PathVariable Long specialistId) {
        return ResponseEntity.ok(walletService.getBalanceByUserId(specialistId));
    }

    // مشاهده تاریخچه تراکنش‌های کیف پول متخصص
    @GetMapping("/{specialistId}/transactions")
    public ResponseEntity<List<WalletTransactionDto>> getTransactions(@PathVariable Long specialistId) {
        var txs = walletService.getTransactionsByUserId(specialistId)
                .stream().map(WalletTransactionMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(txs);
    }

}
