package ir.maktab127.service;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final CustomerRepository customerRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, WalletService walletService, CustomerRepository customerRepository, ServiceCategoryRepository serviceCategoryRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.walletService = walletService;
    }
    @Override
    public Order save(Order order) { return orderRepository.save(order); }
    @Override
    public Optional<Order> findById(Long id) { return orderRepository.findById(id); }
    @Override
    public List<Order> getAll() { return orderRepository.findAll(); }
    @Override
    public void delete(Long id) { orderRepository.findById(id).ifPresent(orderRepository::delete); }
    @Override
    public void completedOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isPresent()  ) {
            order.get().setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order.get());

        }


    }
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
        order.setStartDate(LocalDateTime.parse(dto.getStartDate()));
        order.setCreatedAt(LocalDateTime.now());
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

}