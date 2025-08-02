package ir.maktab127.service;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.dto.OrderResponseDto;
import ir.maktab127.dto.ServiceHistoryDetailDto;
import ir.maktab127.dto.ServiceHistoryFilterDto;
import ir.maktab127.dto.order.OrderSummaryDTO;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.exception.OrderException;
import ir.maktab127.exception.WalletException;
import ir.maktab127.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testGetServiceHistorySummary_withCustomerUserType() {
        ServiceHistoryFilterDto filter = new ServiceHistoryFilterDto();
        filter.setUserType("CUSTOMER");
        filter.setUserId(1L);
        filter.setStartDate(LocalDateTime.now().minusDays(5));
        filter.setEndDate(LocalDateTime.now());
        filter.setOrderStatus(OrderStatus.COMPLETED);
        filter.setServiceCategoryId(10L);

        Pageable pageable = PageRequest.of(0, 10);

        // Mock orderRepository.findAll to return an empty page or sample data
        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        Page<OrderSummaryDTO> result = orderService.getServiceHistorySummary(filter, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        // verify findAll called once
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetServiceHistorySummary_withSpecialistUserType() {
        ServiceHistoryFilterDto filter = new ServiceHistoryFilterDto();
        filter.setUserType("SPECIALIST");
        filter.setUserId(2L);

        Pageable pageable = PageRequest.of(0, 5);

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        Page<OrderSummaryDTO> result = orderService.getServiceHistorySummary(filter, pageable);

        assertNotNull(result);
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetServiceHistoryDetail_found() {
        Long orderId = 1L;


        Order order = new Order();
        order.setId(orderId);
        order.setDescription("desc");
        order.setProposedPrice(BigDecimal.valueOf(100000));
        order.setStartDate(LocalDateTime.now().minusDays(1));
        order.setCreateDate(LocalDateTime.now().minusDays(2));
        order.setAddress("Some Address");
        order.setStatus(OrderStatus.COMPLETED);

        Customer customer = new Customer();
        customer.setId(5L);
        customer.setFirstName("Ali");
        customer.setLastName("Rezaei");
        customer.setEmail("ali@example.com");
        order.setCustomer(customer);

        ServiceCategory service = new ServiceCategory();
        service.setId(3L);
        service.setName("ServiceName");
        service.setDescription("ServiceDesc");
        order.setService(service);

        Proposal proposal = new Proposal();
        proposal.setStatus(ProposalStatus.ACCEPTED);
        Specialist specialist = new Specialist();
        specialist.setId(9L);
        specialist.setFirstName("Sara");
        specialist.setLastName("Ahmadi");
        specialist.setEmail("sara@example.com");
        proposal.setSpecialist(specialist);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(proposalRepository.findByOrderIdAndStatus(orderId, ProposalStatus.ACCEPTED))
                .thenReturn(Optional.of(proposal));

        when(proposalRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());
        when(commentRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

        ServiceHistoryDetailDto detail = orderService.getServiceHistoryDetail(orderId);

        assertNotNull(detail);
        assertEquals(orderId, detail.getOrderId());
        assertEquals("Ali Rezaei", detail.getCustomerName());
        assertEquals("Sara Ahmadi", detail.getSpecialistName());
        verify(orderRepository).findById(orderId);
        verify(proposalRepository).findByOrderIdAndStatus(orderId, ProposalStatus.ACCEPTED);
    }
    @Test
    void testGetOrderHistory() {
        String email = "customer@example.com";
        Customer customer = new Customer();
        customer.setEmail(email);

        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = new ArrayList<>();
        orders.add(new Order());
        Page<Order> orderPage = new PageImpl<>(orders);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer, pageable)).thenReturn(orderPage);

        Page<OrderResponseDto> result = orderService.getOrderHistory(email, pageable);

        assertNotNull(result);
        verify(customerRepository).findByEmail(email);
        verify(orderRepository).findByCustomer(customer, pageable);
    }
    @Test
    void testGetOrderHistoryByStatus() {
        String email = "customer@example.com";
        OrderStatus status = OrderStatus.COMPLETED;
        Customer customer = new Customer();
        customer.setEmail(email);

        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = new ArrayList<>();
        orders.add(new Order());
        Page<Order> orderPage = new PageImpl<>(orders);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerAndStatus(customer, status, pageable)).thenReturn(orderPage);

        Page<OrderResponseDto> result = orderService.getOrderHistoryByStatus(email, status, pageable);

        assertNotNull(result);
        verify(customerRepository).findByEmail(email);
        verify(orderRepository).findByCustomerAndStatus(customer, status, pageable);
    }
}
