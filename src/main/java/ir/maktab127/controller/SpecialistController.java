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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize(  "hasRole('ADMIN')")
    public Page<SpecialistResponseDto> getAll(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "1") int size,
                                              @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        return  specialistService.getAll(pageable).map( SpecialistMapper::toResponseDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specialistService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //
    @PutMapping("/update-info")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Void> updateInfo( @Valid @RequestBody SpecialistUpdateDto dto) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        try {
            specialistService.updateInfo(specialist.getId(), dto);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null); // Conflict
        }
    }
    @PostMapping(value = "/update-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<?> updateProfileImage(
            @RequestPart("profileImage") MultipartFile profileImage) throws IOException {


        Specialist specialist = (Specialist) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        if (profileImage != null && !profileImage.isEmpty()) {
            if (profileImage.getSize() > 300*1024) {
                return ResponseEntity.badRequest().body("Max image size is 300 KB");
            }
            String base64Image = Base64.getEncoder().encodeToString(profileImage.getBytes());


            specialistService.updateProfileImage(specialist.getId(), base64Image);
        }
        return ResponseEntity.ok("Profile image updated");
    }
    //offer to do a service
    @PostMapping("/addProposal")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<ProposalResponseDto> registerProposal(@Valid @RequestBody ProposalRegisterDto dto) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        dto.setSpecialistId(specialist.getId());
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
    @GetMapping("/available-orders")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Page<Order>> getAvailableOrders(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "1") int size,
                                                          @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        Page<Order> availableOrders = specialistService.getAvailableOrdersForSpecialist(specialist.getId(),pageable);
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
    @GetMapping("/proposals")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Page<ProposalResponseDto>> getSpecialistProposals(@RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "1") int size,
                                                                            @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        Page<Proposal> proposals = specialistService.getSpecialistProposals(specialist.getId(), pageable);
        Page<ProposalResponseDto> responseDtos = proposals.map(ProposalMapper::toResponseDto);
        return ResponseEntity.ok(responseDtos);
    }

    // Get Proposal by ID
    @GetMapping("/proposals/{proposalId}")
    @PreAuthorize("hasRole('SPECIALIST')")
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
    @GetMapping("/orders/history")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Page<OrderResponseDto>> getOrderHistory(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "1") int size,
                                                                  @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        Page<Proposal> proposals = proposalService.getProposalsBySpecialist(specialist.getId(), pageable);
        List<OrderResponseDto> dtos = proposals.getContent().stream()
                .map(Proposal::getOrder) // از Proposal به Order
                .map(OrderMapper::toResponseDto)
                .toList();
        Page<OrderResponseDto> ordersDto=new PageImpl<>(dtos,pageable,dtos.size());
        return ResponseEntity.ok(ordersDto);
    }

    //comment and averge rating
    @GetMapping("/rating/average")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<ApiResponseDto> getAverageRating(
    ) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();


        Double avg = commentService.getAverageRatingForSpecialist(specialist.getId());
        return ResponseEntity.ok(new ApiResponseDto("Your rating is: "+avg,true));
    }

    // مشاهده امتیاز سفارش خاص
    @GetMapping("/orders/rating/{orderId}")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<ApiResponseDto> getOrderRating( @PathVariable Long orderId) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        Integer rating = commentService.getOrderRatingForSpecialist(specialist.getId(), orderId);
        return ResponseEntity.ok(new ApiResponseDto("your rating is :"+rating.toString(),true));
    }

    //کیف پول
    @GetMapping("/balance")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<ApiResponseDto> getBalance() {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(new ApiResponseDto("ballence :"+walletService.getBalanceByUserId(specialist.getId()),true));
    }

    // مشاهده تاریخچه تراکنش‌های کیف پول متخصص
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Page<WalletTransactionDto>> getTransactions(@RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "1") int size,
                                                                      @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Specialist specialist= specialistService.findByEmail(email).orElseThrow();
        Page<WalletTransactionDto> txs = walletService.getTransactionsByUserId(specialist.getId(),pageable).map(  WalletTransactionMapper::toDto);

        return ResponseEntity.ok(txs);
    }

}
