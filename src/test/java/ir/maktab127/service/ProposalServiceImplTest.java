package ir.maktab127.service;

import ir.maktab127.dto.ProposalRegisterDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.ProposalRepository;
import ir.maktab127.repository.SpecialistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProposalServiceImplTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProposalServiceImpl proposalService;

    private Specialist specialist;
    private Order order;
    private Proposal proposal;
    private ProposalRegisterDto dto;
    private ServiceCategory serviceCategory;

    @BeforeEach
    void setUp() {
        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);

        specialist = new Specialist();
        specialist.setId(1L);
        specialist.setServiceCategories(Collections.singletonList(serviceCategory));

        order = new Order();
        order.setId(1L);
        order.setService(serviceCategory); // استفاده از شیء معتبر ServiceCategory
        order.setStatus(OrderStatus.WAITING_FOR_PROPOSAL);
        order.setStartDate(LocalDateTime.now().plusDays(1));
        order.setProposedPrice(new BigDecimal("100.00"));
        order.setDescription("Test order");
        order.setAddress("Test address");

        proposal = new Proposal();
        proposal.setId(1L);
        proposal.setSpecialist(specialist);
        proposal.setOrder(order);
        proposal.setProposedPrice(new BigDecimal("90.00"));
        proposal.setProposedStartTime(LocalDateTime.now().plusDays(2));
        proposal.setEndDate(LocalDateTime.now().plusDays(3));
        proposal.setStatus(ProposalStatus.PENDING);

        dto = new ProposalRegisterDto();
        dto.setSpecialistId(1L);
        dto.setOrderId(1L);
        dto.setProposedPrice(new BigDecimal("90.00"));
        dto.setStartDate(LocalDateTime.now().plusDays(2));
        dto.setEndDate(LocalDateTime.now().plusDays(3));
    }

    @Test
    void save_ValidProposal_ReturnsSavedProposal() {
        when(proposalRepository.save(proposal)).thenReturn(proposal);

        Proposal result = proposalService.save(proposal);

        assertNotNull(result);
        assertEquals(proposal, result);
        verify(proposalRepository, times(1)).save(proposal);
    }

    @Test
    void findById_Exists_ReturnsProposal() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        Optional<Proposal> result = proposalService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(proposal, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Proposal> result = proposalService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsAllProposals() {
        List<Proposal> proposals = Arrays.asList(proposal);
        when(proposalRepository.findAll()).thenReturn(proposals);

        List<Proposal> result = proposalService.getAll();

        assertEquals(1, result.size());
        assertEquals(proposal, result.get(0));
    }

    @Test
    void delete_ProposalExists_DeletesSuccessfully() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        proposalService.delete(1L);

        verify(proposalRepository, times(1)).delete(proposal);
    }

    @Test
    void delete_ProposalNotExists_NoAction() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.empty());

        proposalService.delete(1L);

        verify(proposalRepository, never()).delete(any());
    }

    @Test
    void registerProposal_ValidDto_Success() {
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalRepository.save(any(Proposal.class))).thenReturn(proposal);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Proposal result = proposalService.registerProposal(dto);

        assertNotNull(result);
        assertEquals(ProposalStatus.PENDING, result.getStatus());
        assertEquals(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(proposalRepository, times(1)).save(any(Proposal.class));
    }

    @Test
    void registerProposal_SpecialistNotFound_ThrowsException() {
        when(specialistRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> proposalService.registerProposal(dto));

        assertEquals("Specialist not found", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(proposalRepository, never()).save(any());
    }

    @Test
    void registerProposal_OrderNotFound_ThrowsException() {
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> proposalService.registerProposal(dto));

        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(proposalRepository, never()).save(any());
    }

    @Test
    void registerProposal_ServiceNotAllowed_ThrowsException() {
        ServiceCategory differentService = new ServiceCategory();
        differentService.setId(2L);
        order.setService(differentService);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposalService.registerProposal(dto));

        assertEquals("Specialist not allowed for this order's service", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(proposalRepository, never()).save(any());
    }

    @Test
    void registerProposal_InvalidDates_ThrowsException() {
        dto.setEndDate(LocalDateTime.now().minusDays(1));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> proposalService.registerProposal(dto));

        assertEquals("Invalid start and end date", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(proposalRepository, never()).save(any());
    }

    @Test
    void getProposalsByOrder_Paged_ReturnsProposals() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Proposal> page = new PageImpl<>(Arrays.asList(proposal));
        when(proposalRepository.findByOrderId(1L, pageable)).thenReturn(page);

        Page<Proposal> result = proposalService.getProposalsByOrder(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(proposal, result.getContent().get(0));
    }

    @Test
    void getProposalsByOrder_List_ReturnsProposals() {
        List<Proposal> proposals = Arrays.asList(proposal);
        when(proposalRepository.findByOrderId(1L)).thenReturn(proposals);

        List<Proposal> result = proposalService.getProposalsByOrder(1L);

        assertEquals(1, result.size());
        assertEquals(proposal, result.get(0));
    }

    @Test
    void getProposalsBySpecialist_ReturnsProposals() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Proposal> page = new PageImpl<>(Arrays.asList(proposal));
        when(proposalRepository.findBySpecialistId(1L, pageable)).thenReturn(page);

        Page<Proposal> result = proposalService.getProposalsBySpecialist(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(proposal, result.getContent().get(0));
    }

    @Test
    void updateProposalStatus_ProposalExists_UpdatesStatus() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(Proposal.class))).thenReturn(proposal);

        proposalService.updateProposalStatus(1L, ProposalStatus.ACCEPTED);

        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
        verify(proposalRepository, times(1)).save(proposal);
    }

    @Test
    void updateProposalStatus_ProposalNotExists_NoAction() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.empty());

        proposalService.updateProposalStatus(1L, ProposalStatus.ACCEPTED);

        verify(proposalRepository, never()).save(any());
    }

    @Test
    void isFirstProposalForOrder_OneProposal_ReturnsTrue() {
        when(proposalRepository.findByOrderId(1L)).thenReturn(Arrays.asList(proposal));

        boolean result = proposalService.isFirstProposalForOrder(1L);

        assertTrue(result);
    }

    @Test
    void isFirstProposalForOrder_MultipleProposals_ReturnsFalse() {
        when(proposalRepository.findByOrderId(1L)).thenReturn(Arrays.asList(proposal, new Proposal()));

        boolean result = proposalService.isFirstProposalForOrder(1L);

        assertFalse(result);
    }

    @Test
    void getProposalByOrderAndSpecialist_Exists_ReturnsProposal() {
        when(proposalRepository.findBySpecialistIdAndOrderId(1L, 1L)).thenReturn(Arrays.asList(proposal));

        Proposal result = proposalService.getProposalByOrderAndSpecialist(1L, 1L);

        assertEquals(proposal, result);
    }

    @Test
    void getProposalByOrderAndSpecialist_NotExists_ThrowsException() {
        when(proposalRepository.findBySpecialistIdAndOrderId(1L, 1L)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> proposalService.getProposalByOrderAndSpecialist(1L, 1L));

        assertEquals("Proposal not found", exception.getMessage());
    }
}