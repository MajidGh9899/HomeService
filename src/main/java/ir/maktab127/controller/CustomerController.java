package ir.maktab127.controller;

import ir.maktab127.dto.*;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@Validated
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ServiceCategoryService serviceCategoryService;
    private final OrderService orderService;
    private final CommentService commentService;
    private final ProposalService proposalService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDto> register(@Valid @RequestBody CustomerRegisterDto dto) {
        Customer customer = CustomerMapper.toEntity(dto);
        Customer saved = customerService.save(customer);
        return ResponseEntity.ok(CustomerMapper.toResponseDto(saved));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);
        return customer.map(c -> ResponseEntity.ok(CustomerMapper.toResponseDto(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //login
    @PostMapping("/login")
    public ResponseEntity<CustomerResponseDto> login(@Valid @RequestBody CustomerLoginDto dto) {
        Optional<Customer> customer = customerService.login(dto.getEmail(), dto.getPassword());
        return customer
                .map(CustomerMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
    //
    @PutMapping("/{id}/update-info")
    public ResponseEntity<Void> updateInfo(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDto dto) {
        customerService.updateInfo(id, dto);
        return ResponseEntity.ok().build();
    }
    //
    @GetMapping("/service-categories")
    public List<ServiceCategoryResponseDto> getAllServiceCategories() {
        return serviceCategoryService.getAll().stream()
                .map(ServiceCategoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    //order
    @PostMapping("/add-Order")
    public ResponseEntity<OrderResponseDto> registerOrder(@Valid @RequestBody OrderRegisterDto dto) {
        try {
            Order order = orderService.registerOrder(dto);
            return ResponseEntity.ok(OrderMapper.toResponseDto(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    //payOrder
    @PutMapping("pay/{orderId}/{SpecialistId}")
    public ResponseEntity<Void> payOrder(@PathVariable Long orderId, @PathVariable Long SpecialistId) {
        try {
            orderService.completedOrder(orderId);
            orderService.payToSpecialist(orderId, SpecialistId);
            return ResponseEntity.ok().build();


        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    //addComment
    @PostMapping("/register/{orderId}")
    public ResponseEntity<CommentResponseDto> registerComment(
            @PathVariable Long orderId,
            @Valid @RequestBody CommentRegisterDto dto) {
        try {
            Comment comment = commentService.registerComment(dto, orderId);
            return ResponseEntity.ok(CommentMapper.toResponseDto(comment));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(409).build();
        }
    }
// customer phase -2

    // مشاهده لیست پیشنهادهای سفارش با مرتب‌سازی
    @GetMapping("/{customerId}/orders/{orderId}/proposals")
    public ResponseEntity<List<ProposalResponseDto>> getOrderProposals(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "price") String sortBy) {
        // بررسی مالکیت سفارش
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || !orderOpt.get().getCustomer().getId().equals(customerId)) {
            return ResponseEntity.notFound().build();
        }
        List<Proposal> proposals = proposalService.getProposalsByOrder(orderId);
        // مرتب‌سازی
        if (sortBy.equalsIgnoreCase("price")) {
            proposals.sort((a, b) -> a.getProposedPrice().compareTo(b.getProposedPrice()));
        } else if (sortBy.equalsIgnoreCase("specialistRating")) {
            proposals.sort((a, b) -> {

                Integer r1 = a.getSpecialist().getComments().stream().mapToInt(Comment::getRating).sum() ;
                Integer r2 = b.getSpecialist().getComments().stream().mapToInt(Comment::getRating).sum();
                return r2.compareTo(r1); // نزولی
            });
        }
        List<ProposalResponseDto> dtos = proposals.stream().map(ProposalMapper::toResponseDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // انتخاب متخصص برای سفارش
    @PostMapping("/{customerId}/orders/{orderId}/select-proposal/{proposalId}")
    public ResponseEntity<Void> selectProposal(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @PathVariable Long proposalId) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        Optional<Proposal> proposalOpt = proposalService.findById(proposalId);
        if (orderOpt.isEmpty() || proposalOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();
        Proposal proposal = proposalOpt.get();
        if (!order.getCustomer().getId().equals(customerId) || !proposal.getOrder().getId().equals(orderId)) {
            return ResponseEntity.badRequest().build();
        }
        // تغییر وضعیت سفارش
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        orderService.save(order);
        // تغییر وضعیت پیشنهاد انتخاب‌شده
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalService.save(proposal);
        // رد سایر پیشنهادها
        proposalService.getProposalsByOrder(orderId).stream()
                .filter(p -> !p.getId().equals(proposalId))
                .forEach(p -> {
                    p.setStatus(ProposalStatus.REJECTED);
                    proposalService.save(p);
                });
        return ResponseEntity.ok().build();
    }

    // اعلام شروع کار توسط مشتری
    @PostMapping("/{customerId}/orders/{orderId}/start")
    public ResponseEntity<Void> startOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime now) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty())
            return ResponseEntity.notFound().build();
        Order order = orderOpt.get();
        if (!order.getCustomer().getId().equals(customerId))
            return ResponseEntity.badRequest().build();
        if (order.getStatus() != OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL)
            return ResponseEntity.badRequest().build();
        // پیدا کردن پیشنهاد پذیرفته‌شده
        Proposal acceptedProposal = proposalService.getProposalsByOrder(orderId).stream()
                .filter(p -> p.getStatus() == ProposalStatus.ACCEPTED)
                .findFirst().orElse(null);
        if (acceptedProposal == null)
            return ResponseEntity.badRequest().build();
//         فقط بعد از زمان پیشنهادی متخصص

        if (now.isBefore(acceptedProposal.getProposedStartTime()))
            return ResponseEntity.badRequest().build();
        order.setStatus(OrderStatus.IN_PROGRESS);
        orderService.save(order);
        return ResponseEntity.ok().build();
    }

    // اعلام پایان کار توسط مشتری
    @PostMapping("/{customerId}/orders/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(
            @PathVariable Long customerId,
            @PathVariable Long orderId) {
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();
        Order order = orderOpt.get();
        if (!order.getCustomer().getId().equals(customerId))
            return ResponseEntity.badRequest().build();
        if (order.getStatus() != OrderStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().build();
        order.setStatus(OrderStatus.COMPLETED);
        orderService.save(order);
        return ResponseEntity.ok().build();
    }

    //
    @PostMapping("/{customerId}/orders/{orderId}/pay")
    public ResponseEntity<Void> payOrder(@PathVariable Long customerId, @PathVariable Long orderId, @RequestBody PaymentRequestDto paymentRequest) {
        try {
            orderService.payOrder(orderId, paymentRequest);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
