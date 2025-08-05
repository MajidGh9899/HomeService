package ir.maktab127.controller;

import ir.maktab127.dto.*;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.entity.user.User;
import ir.maktab127.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;

@RestController
@RequestMapping("/api/customer")
@Validated
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SpecialistService specialistService;
    private final ServiceCategoryService serviceCategoryService;
    private final OrderService orderService;
    private final CommentService commentService;
    private final ProposalService proposalService;
    private final WalletService walletService;


    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);
        return customer.map(c -> ResponseEntity.ok(CustomerMapper.toResponseDto(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    //
    @PutMapping("/update-info")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDto> updateInfo( @Valid @RequestBody CustomerUpdateDto dto) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();

        customerService.updateInfo(email, dto);
        return ResponseEntity.ok(new ApiResponseDto( "Update Customer successfully.",true));
    }

    @GetMapping("/service-categories")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<ServiceCategoryResponseDto> getAllServiceCategories(@RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "1") int size,
                                                                    @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<ServiceCategory> allServices = serviceCategoryService.getAll(pageable);
        return allServices.map(ServiceCategoryMapper::toResponseDto);
    }

    //order
    @PostMapping("/add-Order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDto> registerOrder(@Valid @RequestBody OrderRegisterDto dto) {
        try {
            String email=SecurityContextHolder.getContext().getAuthentication().getName();
            Customer customer = customerService.findByEmail(email).orElseThrow();
            dto.setCustomerId(customer.getId());
            Order order = orderService.registerOrder(dto);
            return ResponseEntity.ok(OrderMapper.toResponseDto(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


// customer phase -2

    // مشاهده لیست پیشنهادهای سفارش با مرتب‌سازی
    @GetMapping("/orders/{orderId}/proposals")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<ProposalResponseDto>> getOrderProposals(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "price") String sortBy,@RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "1") int size,
    @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer=customerService.findByEmail(email).orElseThrow();
        // بررسی مالکیت سفارش
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || !orderOpt.get().getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.notFound().build();
        }
        List<Proposal> proposals = proposalService.getProposalsByOrder(orderId);
        // مرتب‌سازی
        sortProposlList(sortBy, proposals);
        List<ProposalResponseDto> dtos = proposals.stream().map(ProposalMapper::toResponseDto).toList();
        Page<ProposalResponseDto> result=new PageImpl<>(dtos,pageable,dtos.size());
        return ResponseEntity.ok(result);
    }

    private static void sortProposlList(String sortBy, List<Proposal> proposals) {
        if (sortBy.equalsIgnoreCase("price")) {
            proposals.sort((a, b) -> a.getProposedPrice().compareTo(b.getProposedPrice()));
        } else if (sortBy.equalsIgnoreCase("specialistRating")) {
            proposals.sort((a, b) -> {

                Integer r1 = a.getSpecialist().getComments().stream().mapToInt(Comment::getRating).sum() ;
                Integer r2 = b.getSpecialist().getComments().stream().mapToInt(Comment::getRating).sum();
                return r2.compareTo(r1); // نزولی
            });
        }
    }

    // انتخاب متخصص برای سفارش
    @PostMapping("/orders/{orderId}/select-proposal/{proposalId}/{specialistId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDto> selectProposal(

            @PathVariable Long orderId,
            @PathVariable Long proposalId,
            @PathVariable Long specialistId) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer=customerService.findByEmail(email).orElseThrow();
        Optional<Order> orderOpt = orderService.findById(orderId);
        Optional<Proposal> proposalOpt = proposalService.findById(proposalId);

        Order order = orderOpt.get();
        Proposal proposal = proposalOpt.get();

        Specialist spec=specialistService.findById(specialistId).orElseThrow();
        order.setSpecialist(spec);
        // تغییر وضعیت سفارش
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        orderService.save(order);
        // تغییر وضعیت پیشنهاد انتخاب‌شده

        proposalService.updateProposalStatus(proposalId, ProposalStatus.ACCEPTED);

        return ResponseEntity.ok(new ApiResponseDto("Proposal"+proposalId+" selected successfully",true));
    }

    // اعلام شروع کار توسط مشتری
    @PostMapping("/orders/{orderId}/start")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDto> startOrder(
            @PathVariable Long orderId
            ) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer=customerService.findByEmail(email).orElseThrow();
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty())
            return ResponseEntity.notFound().build();
        Order order = orderOpt.get();
        if (!order.getCustomer().getId().equals(customer.getId()))
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

        if (LocalDateTime.now().isBefore(acceptedProposal.getProposedStartTime()))
            return ResponseEntity.badRequest().build();
        order.setStatus(OrderStatus.IN_PROGRESS);
        orderService.save(order);
        return ResponseEntity.ok(new ApiResponseDto("Order started successfully",true));
    }


    @PutMapping("/orders/{orderId}/complete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDto> completeOrder(
            @PathVariable Long orderId) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer=customerService.findByEmail(email).orElseThrow();
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();
        Order order = orderOpt.get();
        if (!order.getCustomer().getId().equals(customer.getId()))
            return ResponseEntity.badRequest().build();
        if (order.getStatus() != OrderStatus.IN_PROGRESS)
            return ResponseEntity.badRequest().build();
        order.setStatus(OrderStatus.COMPLETED);
        orderService.save(order);
        return ResponseEntity.ok(new ApiResponseDto("order completed",true));
    }

    //
    @PostMapping("/orders/{orderId}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> payOrder( @PathVariable Long orderId, @RequestBody PaymentRequestDto paymentRequest) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer=customerService.findByEmail(email).orElseThrow();
        paymentRequest.setCustomerId(customer.getId());
        try {
            orderService.payOrder(orderId, paymentRequest);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/orders/{orderId}/comment")
    public ResponseEntity<ApiResponseDto> addComment( @PathVariable Long orderId, @RequestBody CommentRegisterDto dto) {
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer=customerService.findByEmail(email).orElseThrow();
        // اعتبارسنجی مالکیت سفارش و وضعیت سفارش
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty() || !orderOpt.get().getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.COMPLETED) {
            return ResponseEntity.badRequest().build();
        }
        // ثبت نظر
        Comment comment = new Comment();
        comment.setCustomer(order.getCustomer());
        comment.setSpecialist(dto.getSpecialistId() != null ? new Specialist() {{ setId(dto.getSpecialistId()); }} : null);
        comment.setRating(dto.getRating());
        comment.setText(dto.getText());
        commentService.save(comment);
        return ResponseEntity.ok(new ApiResponseDto("Comment added successfully",true));
    }
    @GetMapping("/balance")
  @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponseDto> getBalance( ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer  customer = customerService.findByEmail( email).orElseThrow();
        return ResponseEntity.ok(new ApiResponseDto("balance: "+walletService.getBalanceByUserId(customer.getId()),true));
    }
    //provider
    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponseDto>> getOrderHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<OrderResponseDto> orders = orderService.getOrderHistory(email, Pageable.ofSize(10));
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/ordersByStatus")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponseDto>> getOrderHistoryByStatus(@RequestParam OrderStatus status) {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();


        Page<OrderResponseDto> orders = orderService.getOrderHistoryByStatus(email, status, Pageable.ofSize(10));
        return ResponseEntity.ok(orders);
    }



}
