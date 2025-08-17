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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private ProposalService proposalService;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private Customer customer;
    private Specialist specialist;
    private ServiceCategory serviceCategory;
    private Proposal proposal;
    private Wallet customerWallet;
    private Wallet specialistWallet;
    private OrderRegisterDto orderRegisterDto;
    private PaymentRequestDto paymentRequestDto;
    private ServiceHistoryFilterDto filterDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setFirstName("John");
        customer.setLastName("Doe");

        specialist = new Specialist();
        specialist.setId(2L);
        specialist.setFirstName("Jane");
        specialist.setLastName("Smith");
        specialist.setEmail("specialist@test.com");
        specialist.setStatus(AccountStatus.APPROVED);
        specialist.setComments(Collections.emptyList());

        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);
        serviceCategory.setBasePrice(new BigDecimal("50.00"));
        serviceCategory.setName("Test Service");
        serviceCategory.setDescription("Test Description");

        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setService(serviceCategory);
        order.setDescription("Test order");
        order.setProposedPrice(new BigDecimal("100.00"));
        order.setAddress("Test address");
        order.setStartDate(LocalDateTime.now().plusDays(1));
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITING_FOR_PROPOSAL);
        order.setSpecialist(specialist);

        proposal = new Proposal();
        proposal.setId(1L);
        proposal.setOrder(order);
        proposal.setSpecialist(specialist);
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setProposedPrice(new BigDecimal("90.00"));
        proposal.setProposedStartTime(LocalDateTime.now().minusHours(1));
        proposal.setEndDate(LocalDateTime.now().minusMinutes(1));

        customerWallet = new Wallet();
        customerWallet.setId(1L);
        customerWallet.setUser(customer);
        customerWallet.setBalance(new BigDecimal("200.00"));

        specialistWallet = new Wallet();
        specialistWallet.setId(2L);
        specialistWallet.setUser(specialist);
        specialistWallet.setBalance(BigDecimal.ZERO);

        orderRegisterDto = new OrderRegisterDto();
        orderRegisterDto.setCustomerId(1L);
        orderRegisterDto.setServiceCategoryId(1L);
        orderRegisterDto.setDescription("Test order");
        orderRegisterDto.setProposedPrice(new BigDecimal("100.00"));
        orderRegisterDto.setAddress("Test address");
        orderRegisterDto.setStartDate(LocalDateTime.now().plusDays(1));

        paymentRequestDto = new PaymentRequestDto();
        paymentRequestDto.setCustomerId(1L);

        filterDto = new ServiceHistoryFilterDto();
        filterDto.setUserType("CUSTOMER");
        filterDto.setUserId(1L);
        filterDto.setStartDate(LocalDateTime.now().minusDays(10));
        filterDto.setEndDate(LocalDateTime.now());
        filterDto.setOrderStatus(OrderStatus.COMPLETED);
        filterDto.setServiceCategoryId(1L);
    }

    @Test
    void save_ValidOrder_ReturnsSavedOrder() {
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.save(order);

        assertNotNull(result);
        assertEquals(order, result);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void findById_Exists_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(order, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsAllOrders() {
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAll();

        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
    }

    @Test
    void delete_OrderExists_DeletesSuccessfully() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.delete(1L);

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void delete_OrderNotExists_NoAction() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        orderService.delete(1L);

        verify(orderRepository, never()).delete((Order) any());
    }

    @Test
    void completedOrder_OrderExists_SetsCompletedStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.completedOrder(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void completedOrder_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.completedOrder(1L));
    }

    @Test
    void registerOrder_ValidDto_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(serviceCategory));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.registerOrder(orderRegisterDto);

        assertNotNull(result);
        assertEquals(OrderStatus.WAITING_FOR_PROPOSAL, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void registerOrder_CustomerNotFound_ThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.registerOrder(orderRegisterDto));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void registerOrder_ServiceNotFound_ThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.registerOrder(orderRegisterDto));

        assertEquals("Service not found", exception.getMessage());
    }

    @Test
    void registerOrder_ProposedPriceTooLow_ThrowsException() {
        orderRegisterDto.setProposedPrice(new BigDecimal("10.00"));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(serviceCategory));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.registerOrder(orderRegisterDto));

        assertEquals("Proposed price must be >= base price", exception.getMessage());
    }

    @Test
    void updateOrderStatus_OrderExists_UpdatesStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateOrderStatus(1L, OrderStatus.IN_PROGRESS);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderStatus_OrderNotExists_NoAction() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        orderService.updateOrderStatus(1L, OrderStatus.IN_PROGRESS);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrdersByStatus_ReturnsOrders() {
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByStatus(OrderStatus.WAITING_FOR_PROPOSAL)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByStatus(OrderStatus.WAITING_FOR_PROPOSAL);

        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
    }

    @Test
    void getOrdersByServiceCategory_ReturnsOrders() {
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByServiceCategoryId(1L)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByServiceCategory(1L);

        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
    }



    @Test
    void payOrder_CustomerNotAuthorized_ThrowsException() {
        paymentRequestDto.setCustomerId(3L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderException exception = assertThrows(OrderException.class,
                () -> orderService.payOrder(1L, paymentRequestDto));

        assertEquals("Customer is not authorized to pay this order", exception.getMessage());
    }

    @Test
    void payOrder_OrderNotCompleted_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderException exception = assertThrows(OrderException.class,
                () -> orderService.payOrder(1L, paymentRequestDto));

        assertEquals("Order is not in COMPLETED status", exception.getMessage());
    }

    @Test
    void payOrder_InsufficientBalance_ThrowsException() {
        order.setStatus(OrderStatus.COMPLETED);
        customerWallet.setBalance(new BigDecimal("50.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(customerWallet));

        WalletException exception = assertThrows(WalletException.class,
                () -> orderService.payOrder(1L, paymentRequestDto));

        assertEquals("Insufficient balance. Please charge your wallet.", exception.getMessage());
    }



    @Test
    void getServiceHistoryDetail_OrderExists_ReturnsDetail() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalRepository.findByOrderIdAndStatus(1L, ProposalStatus.ACCEPTED)).thenReturn(Optional.of(proposal));
        when(proposalRepository.findByOrderId(1L)).thenReturn(Arrays.asList(proposal));
        when(commentRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());

        ServiceHistoryDetailDto result = orderService.getServiceHistoryDetail(1L);

        assertEquals(1L, result.getOrderId());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("Jane Smith", result.getSpecialistName());
        assertEquals("Test Service", result.getServiceName());
    }

    @Test
    void getServiceHistoryDetail_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getServiceHistoryDetail(1L));

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void getOrderHistory_ValidEmail_ReturnsOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer, pageable)).thenReturn(page);

        Page<OrderResponseDto> result = orderService.getOrderHistory("customer@test.com", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrderHistory_CustomerNotFound_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrderHistory("customer@test.com", pageable));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void getOrderHistoryByStatus_ValidEmailAndStatus_ReturnsOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Arrays.asList(order));
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerAndStatus(customer, OrderStatus.WAITING_FOR_PROPOSAL, pageable)).thenReturn(page);

        Page<OrderResponseDto> result = orderService.getOrderHistoryByStatus("customer@test.com", OrderStatus.WAITING_FOR_PROPOSAL, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrderHistoryByStatus_CustomerNotFound_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.findByEmail("customer@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrderHistoryByStatus("customer@test.com", OrderStatus.WAITING_FOR_PROPOSAL, pageable));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void startOrder_ValidOrderAndCustomer_Success() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalService.getProposalsByOrder(1L)).thenReturn(Arrays.asList(proposal));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.startOrder(1L, customer);

        assertEquals(OrderStatus.IN_PROGRESS, result.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void startOrder_WrongStatus_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderService.startOrder(1L, customer));

        assertEquals("Order must be in WAITING_FOR_SPECIALIST_ARRIVAL status to start.", exception.getMessage());
    }

    @Test
    void startOrder_WrongCustomer_ThrowsException() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        Customer wrongCustomer = new Customer();
        wrongCustomer.setId(3L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderService.startOrder(1L, wrongCustomer));

        assertEquals("Customer must be the owner of the order to start.", exception.getMessage());
    }

    @Test
    void startOrder_NoAcceptedProposal_ThrowsException() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalService.getProposalsByOrder(1L)).thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> orderService.startOrder(1L, customer));
    }

    @Test
    void startOrder_BeforeProposedStartTime_ThrowsException() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL);
        proposal.setProposedStartTime(LocalDateTime.now().plusDays(1));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalService.getProposalsByOrder(1L)).thenReturn(Arrays.asList(proposal));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderService.startOrder(1L, customer));

        assertTrue(exception.getMessage().startsWith("Cannot start the order before the proposed start time"));
    }

    @Test
    void selectProposal_ValidData_Success() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalService.findById(1L)).thenReturn(Optional.of(proposal));
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.selectProposal(1L, 1L, 2L);

        assertEquals(OrderStatus.WAITING_FOR_SPECIALIST_ARRIVAL, result.getStatus());
        assertEquals(proposal, result.getAcceptedProposal());
        assertEquals(specialist, result.getSpecialist());
        verify(proposalService, times(1)).updateProposalStatus(1L, ProposalStatus.ACCEPTED);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void selectProposal_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.selectProposal(1L, 1L, 2L));
    }

    @Test
    void selectProposal_WrongStatus_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderService.selectProposal(1L, 1L, 2L));

        assertEquals("Order must be in WAITING_FOR_SPECIALIST_SELECTION status to select a proposal.", exception.getMessage());
    }

    @Test
    void selectProposal_ProposalNotFound_ThrowsException() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalService.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.selectProposal(1L, 1L, 2L));
    }

    @Test
    void selectProposal_SpecialistNotFound_ThrowsException() {
        order.setStatus(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalService.findById(1L)).thenReturn(Optional.of(proposal));
        when(specialistRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.selectProposal(1L, 1L, 2L));
    }
}