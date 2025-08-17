package ir.maktab127.service;

import ir.maktab127.dto.*;
import ir.maktab127.dto.order.OrderSummaryDTO;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.exception.OrderException;
import ir.maktab127.exception.WalletException;
import ir.maktab127.repository.*;
import ir.maktab127.repository.specification.OrderSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final WalletService walletService;

    private final CustomerRepository customerRepository;

    private final ServiceCategoryRepository serviceCategoryRepository;

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ProposalService proposalService;
    private final SpecialistRepository specialistRepository;
    private final ProposalRepository proposalRepository;
    private final CommentRepository commentRepository;



    @Transactional
    @Override
    public Order save(Order order) { return orderRepository.save(order); }
    @Override
    public Optional<Order> findById(Long id) { return orderRepository.findById(id); }
    @Override
    public List<Order> getAll() { return orderRepository.findAll(); }
    @Override
    public void delete(Long id) { orderRepository.findById(id).ifPresent(orderRepository::delete); }
    @Transactional
    @Override
    public void completedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

        }



    @Transactional
    @Override
    public Order registerOrder(OrderRegisterDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        ServiceCategory service = serviceCategoryRepository.findById(dto.getServiceCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));


        if (dto.getProposedPrice().compareTo(service.getBasePrice()) < 0)
            throw new IllegalArgumentException("Proposed price must be >= base price");

        Order order = new Order();
        order.setCustomer(customer);
        order.setService(service);
        order.setDescription(dto.getDescription());
        order.setProposedPrice(dto.getProposedPrice());
        order.setAddress(dto.getAddress());
        order.setStartDate(dto.getStartDate());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITING_FOR_PROPOSAL);
        return orderRepository.save(order);
    }

    //
    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> getOrdersByServiceCategory(Long serviceCategoryId) {
        return orderRepository.findByServiceCategoryId(serviceCategoryId);
    }

    //phase 3

    @Transactional
    public void payOrder(Long orderId, PaymentRequestDto paymentRequest) {
        // اعتبارسنجی ورودی
        if (paymentRequest.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }

        // یافتن سفارش
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found"));

        // بررسی مالکیت سفارش
        if (!order.getCustomer().getId().equals(paymentRequest.getCustomerId())) {
            throw new OrderException("Customer is not authorized to pay this order");
        }

        // بررسی وضعیت سفارش
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new OrderException("Order is not in COMPLETED status");
        }

        // یافتن کیف پول مشتری
        Wallet customerWallet = walletRepository.findByUserId(paymentRequest.getCustomerId())
                .orElseThrow(() -> new WalletException("Customer wallet not found"));

        // یافتن متخصص و کیف پول او
        Specialist specialist = order.getSpecialist();
        if (specialist == null) {
            throw new OrderException("Specialist not assigned to this order");
        }
        Wallet specialistWallet = walletRepository.findByUserId(specialist.getId())
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(specialist);
                    newWallet.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });

        // بررسی موجودی
        BigDecimal price = order.getProposedPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException("Invalid order price");
        }
        if (customerWallet.getBalance().compareTo(price) < 0) {
            throw new WalletException("Insufficient balance. Please charge your wallet.");
        }

        // کسر از کیف پول مشتری
        customerWallet.setBalance(customerWallet.getBalance().subtract(price));
        walletRepository.save(customerWallet);

        // واریز ۷۰٪ به کیف پول متخصص
        BigDecimal specialistShare = price.multiply(BigDecimal.valueOf(0.7));
        specialistWallet.setBalance(specialistWallet.getBalance().add(specialistShare));
        walletRepository.save(specialistWallet);

        // ثبت تراکنش برای مشتری
        WalletTransaction customerTx = new WalletTransaction();
        customerTx.setWallet(customerWallet);
        customerTx.setAmount(price.negate());
        customerTx.setCreateDate(LocalDateTime.now());
        customerTx.setDescription("Payment for order ID: " + orderId);
        walletTransactionRepository.save(customerTx);

        // ثبت تراکنش برای متخصص
        WalletTransaction specialistTx = new WalletTransaction();
        specialistTx.setWallet(specialistWallet);
        specialistTx.setAmount(specialistShare);
        specialistTx.setCreateDate(LocalDateTime.now());
        specialistTx.setDescription("Payment received for order ID: " + orderId);
        walletTransactionRepository.save(specialistTx);

        // به‌روزرسانی وضعیت سفارش
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // جریمه تأخیر
        Proposal proposal = proposalService.getProposalByOrderAndSpecialist(orderId, specialist.getId());
        if (proposal == null) {
            throw new OrderException("Proposal not found for order and specialist");
        }
        LocalDateTime proposedEndTime = proposal.getEndDate();
        LocalDateTime actualEndTime = LocalDateTime.now();
        long delayHours = Duration.between(proposedEndTime, actualEndTime).toHours();

        if (delayHours > 0) {
            int negativePoints = (int) delayHours;
            int currentRating = specialist.getComments().stream().mapToInt(Comment::getRating).sum() != 0 ? specialist.getComments().stream().mapToInt(Comment::getRating).sum() : 0;
            int newRating = currentRating - negativePoints;
            Comment  comment = new Comment();
            comment.setRating(newRating);
            comment.setSpecialist(specialist);
            comment.setCustomer(order.getCustomer());
            comment.setOrder(order);

            if (newRating < 0) {
                specialist.setStatus(AccountStatus.DEACTIVATED);
            }
            specialistRepository.save(specialist);


        }
    }

    @Override
    public Page<OrderSummaryDTO> getServiceHistorySummary(ServiceHistoryFilterDto filter, Pageable pageable) {
        LocalDateTime startDate = filter.getStartDate();
        LocalDateTime endDate = filter.getEndDate();
        OrderStatus status = filter.getOrderStatus();
        Long serviceCategoryId = filter.getServiceCategoryId();
        Long customerId=null;
        Long specialistId=null;
        if(filter.getUserType().equals("CUSTOMER"))
             customerId = filter.getUserId();
        if(filter.getUserType().equals("SPECIALIST"))
             specialistId = filter.getUserId();
        Specification<Order> spec = getOrderSpecification(startDate, endDate, status, serviceCategoryId, customerId, specialistId);
        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return   orders.map(OrderMapper::toSummaryDto);
    }

    private static Specification<Order> getOrderSpecification(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status, Long serviceCategoryId, Long customerId, Long specialistId) {
        Specification<Order> spec =    Specification.not(null);

        if (startDate != null)
            spec = spec.and(OrderSpecification.hasStartDate(startDate));

        if (endDate != null)
            spec = spec.and(OrderSpecification.hasEndDate(endDate));

        if (status != null)
            spec = spec.and(OrderSpecification.hasStatus(status));

        if (serviceCategoryId != null)
            spec = spec.and(OrderSpecification.hasServiceCategoryId(serviceCategoryId));

        if (customerId != null)
            spec = spec.and(OrderSpecification.hasCustomerId(customerId));

        if (specialistId != null)
            spec = spec.and(OrderSpecification.hasAcceptedProposalBySpecialist(specialistId));
        return spec;
    }

    @Override
    public ServiceHistoryDetailDto getServiceHistoryDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        ServiceHistoryDetailDto detailDto = getServiceHistoryDetailDto(order);


        setcustomer(detailDto, order);


        Optional<Proposal> acceptedProposal = proposalRepository.findByOrderIdAndStatus(order.getId(), ProposalStatus.ACCEPTED);
        if (acceptedProposal.isPresent()) {
            Specialist specialist = acceptedProposal.get().getSpecialist();
            detailDto.setSpecialistId(specialist.getId());
            detailDto.setSpecialistName(specialist.getFirstName() + " " + specialist.getLastName());
            detailDto.setSpecialistEmail(specialist.getEmail());

        }


        detailDto.setServiceId(order.getService().getId());
        detailDto.setServiceName(order.getService().getName());
        detailDto.setServiceDescription(order.getService().getDescription());


        List<Proposal> proposals = proposalRepository.findByOrderId(order.getId());
        List<ProposalResponseDto> proposalDtos = proposals.stream()
                .map(ProposalMapper::toResponseDto)
                .collect(Collectors.toList());
        detailDto.setProposals(proposalDtos);


        List<Comment> comments = commentRepository.findByOrderId(order.getId());
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentMapper::toResponseDto)
                .collect(Collectors.toList());
        detailDto.setComments(commentDtos);

        return detailDto;
    }

    private static void setcustomer(ServiceHistoryDetailDto detailDto, Order order) {
        detailDto.setCustomerId(order.getCustomer().getId());
        detailDto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        detailDto.setCustomerEmail(order.getCustomer().getEmail());
    }

    private static ServiceHistoryDetailDto getServiceHistoryDetailDto(Order order) {
        ServiceHistoryDetailDto detailDto = new ServiceHistoryDetailDto();
        detailDto.setOrderId(order.getId());
        detailDto.setDescription(order.getDescription());
        detailDto.setProposedPrice(order.getProposedPrice());
        detailDto.setStartDate(order.getStartDate());
        detailDto.setCreatedAt(order.getCreateDate());
        detailDto.setAddress(order.getAddress());
        detailDto.setStatus(order.getStatus());
        return detailDto;
    }

    @Override
    @Transactional
    public Page<OrderResponseDto> getOrderHistory(String email, Pageable Page) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Page<Order> order = orderRepository.findByCustomer(customer,Page);
        return order.map(OrderMapper::toResponseDto);
    }
    @Override
    @Transactional
    public Page<OrderResponseDto> getOrderHistoryByStatus(String Email, OrderStatus status, Pageable pageable) {
        Customer customer = customerRepository.findByEmail(Email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

          Page<Order> order = orderRepository.findByCustomerAndStatus( customer, status, pageable);
          return order.map(OrderMapper::toResponseDto);
    }

    @Override
    @Transactional
    public Order startOrder(Long orderId, Customer customer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        // Verify the order is in the correct state
        if (order.getStatus() != OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL) {
            throw new IllegalStateException("Order must be in WAITING_FOR_SPECIALIST_ARRIVAL status to start.");
        }
        if(order.getCustomer()!=customer){
            throw new IllegalStateException("Customer must be the owner of the order to start.");
        }


        Proposal acceptedProposal = proposalService.getProposalsByOrder(orderId).stream()
                .filter(p -> p.getStatus() == ProposalStatus.ACCEPTED).findFirst().orElseThrow();

        // Verify the current time is after the proposed start time
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime proposedStartTime = acceptedProposal.getProposedStartTime();
        if (currentTime.isBefore(proposedStartTime)) {
            throw new IllegalStateException("Cannot start the order before the proposed start time: " + proposedStartTime);
        }

        // Update the order status to IN_PROGRess
        order.setStatus(OrderStatus.IN_PROGRESS);
        return orderRepository.save(order);
    }
    @Transactional
    @Override
    public Order selectProposal(Long orderId, Long proposalId, Long specialistId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();



        // Verify the order is in the correct state
        if (order.getStatus() != OrderStatus.WAITING_FOR_SPECIALIST_SELECTION) {
            throw new IllegalStateException("Order must be in WAITING_FOR_SPECIALIST_SELECTION status to select a proposal.");
        }





        // Update the proposal status
        proposalService.updateProposalStatus(proposalId, ProposalStatus.ACCEPTED);

        Proposal proposal = proposalService.findById(proposalId).orElseThrow();
        // Update the order
        order.setAcceptedProposal(proposal);
        order.setSpecialist(specialistRepository.findById(specialistId).orElseThrow());
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        return orderRepository.save(order);
    }
    //clean code
    //validation controller
    //format jason
    //exption handler//pageable

}