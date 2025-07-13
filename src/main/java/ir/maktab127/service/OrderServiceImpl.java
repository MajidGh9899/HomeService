package ir.maktab127.service;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ServiceCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

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

}