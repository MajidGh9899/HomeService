package ir.maktab127.service;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.exception.OrderException;
import ir.maktab127.exception.WalletException;
import ir.maktab127.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isPresent()  ) {
            order.get().setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order.get());

        }


    }
    @Override
    @Transactional
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
        order.setStartDate(LocalDateTime.parse(dto.getStartDate()));
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITING_FOR_PROPOSAL);
        return orderRepository.save(order);
    }
    @Override
    public void payToSpecialist(Long orderId, Long specialistId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            if (order.get().getStatus() == OrderStatus.COMPLETED) {
                try {
                walletService.withdrawFromCustomer(order.get().getCustomer().getId(), order.get().getProposedPrice());

                } catch (RuntimeException e) {
                    throw new RuntimeException("Not Enough Money");
                }
                walletService.depositToSpecialist(specialistId, order.get().getProposedPrice());
            }
        }

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
    }}