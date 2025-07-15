package ir.maktab127.service;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
    @Override
    public void payOrder(Long orderId, PaymentRequestDto paymentRequest) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.COMPLETED)
            throw new RuntimeException("Order is not completed");
        Wallet customerWallet = walletRepository.findByUserId(paymentRequest.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer wallet not found"));
        Specialist specialist =  order.getSpecialist();
        Wallet specialistWallet = walletRepository.findByUserId(specialist.getId()).orElseThrow(() -> new RuntimeException("Specialist wallet not found"));
        BigDecimal price = order.getProposedPrice();
        if (customerWallet.getBalance().compareTo(price) < 0)
            throw new RuntimeException("Insufficient balance Please Charge it");
        // کسر از کیف پول مشتری
        customerWallet.setBalance(customerWallet.getBalance().subtract(price));
        walletRepository.save(customerWallet);
        // واریز ۷۰٪ به متخصص
        BigDecimal specialistShare = price.multiply(BigDecimal.valueOf(0.7));
        specialistWallet.setBalance(specialistWallet.getBalance().add(specialistShare));
        walletRepository.save(specialistWallet);
        //   ثبت تراکنش برای مشتری و متخصص
        WalletTransaction tx1 = new WalletTransaction();
        tx1.setWallet(customerWallet);
        tx1.setAmount(price.negate());
        tx1.setCreateDate(LocalDateTime.now());
        tx1.setDescription("pay to order id  :  " + orderId);
        walletTransactionRepository.save(tx1);

        WalletTransaction tx2 = new WalletTransaction();
        tx2.setWallet(specialistWallet);
        tx2.setAmount(specialistShare);
        tx2.setCreateDate(LocalDateTime.now());
        tx2.setDescription("for Done order id  : " + orderId);
        walletTransactionRepository.save(tx2);
        // جریمه تأخیر
        LocalDateTime proposedSpecialistEndTime = proposalService.getProposalByOrderAndSpecialist(orderId, specialist.getId()).getEndDate(); // فرض: مدت سفارش ۸ ساعت
        LocalDateTime actualEnd = LocalDateTime.now(); // فرض: زمان پایان الان
        long delayHours = Duration.between(proposedSpecialistEndTime, actualEnd).toHours();
        if (delayHours > 0) {
            int negativePoints = (int) delayHours;
            int newRating = (specialist.getComments().stream().mapToInt(Comment::getRating).average() != null ? specialist.getComments().stream().mapToInt(Comment::getRating).sum() : 0) - negativePoints;
           // specialist.setRating(newRating);
            Comment comment = new Comment();

            if (newRating < 0)
                specialist.setStatus(AccountStatus.DEACTIVATED);
        }

    }

}