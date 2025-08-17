package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Role;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.*;
import jakarta.mail.MessagingException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpecialistServiceImplTest {

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private SpecialistServiceImpl specialistService;

    private Specialist specialist;
    private Order order;
    private Proposal proposal;
    private Customer customer;
    private ServiceCategory serviceCategory;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        specialist = new Specialist();
        specialist.setId(1L);
        specialist.setEmail("test@specialist.com");
        specialist.setPassword("password");
        specialist.setStatus(AccountStatus.NEW);
        specialist.setRoles(Set.of(Role.SPECIALIST));
        specialist.setEmailVerificationToken(UUID.randomUUID().toString());
        specialist.setEmailVerified(false);

        customer = new Customer();
        customer.setId(2L);

        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);

        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setService(serviceCategory);
        order.setStatus(OrderStatus.WAITING_FOR_PROPOSAL);
        order.setDescription("Test order");
        order.setProposedPrice(new BigDecimal("100.00"));
        order.setStartDate(LocalDateTime.now());
        order.setAddress("Test address");

        proposal = new Proposal();
        proposal.setId(1L);
        proposal.setOrder(order);
        proposal.setSpecialist(specialist);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setProposedPrice(new BigDecimal("90.00"));
        proposal.setProposedStartTime(LocalDateTime.now());


        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(specialist);
        wallet.setBalance(BigDecimal.ZERO);
    }

    // تست متد register
    @Test
    void register_ValidSpecialist_Success() throws MessagingException {

        when(specialistRepository.findByEmail("test@specialist.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(specialistRepository.save(any(Specialist.class))).thenReturn(specialist);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletTransactionRepository.save(any(WalletTransaction.class))).thenReturn(new WalletTransaction());


        Specialist result = specialistService.register(specialist);


        assertNotNull(result);
        assertEquals(AccountStatus.NEW, result.getStatus());
        assertEquals("encodedPassword", result.getPassword());
        assertFalse(result.isEmailVerified());
        assertNotNull(result.getEmailVerificationToken());
        assertEquals(Set.of(Role.SPECIALIST), result.getRoles());
        assertNotNull(result.getWallet());
        verify(emailService, times(1)).sendVerificationEmail("test@specialist.com", result.getEmailVerificationToken());
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(walletTransactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() throws MessagingException {

        when(specialistRepository.findByEmail("test@specialist.com")).thenReturn(Optional.of(specialist));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> specialistService.register(specialist));
        assertEquals("Email already exists", exception.getMessage());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    // تست متد verifyEmail
    @Test
    void verifyEmail_ValidToken_Success() {

        when(specialistRepository.findByEmailVerificationToken(specialist.getEmailVerificationToken()))
                .thenReturn(Optional.of(specialist));
        when(specialistRepository.save(any(Specialist.class))).thenReturn(specialist);

        specialistService.verifyEmail(specialist.getEmailVerificationToken());

        assertTrue(specialist.isEmailVerified());
        assertNull(specialist.getEmailVerificationToken());
        assertEquals(AccountStatus.NEW, specialist.getStatus()); // زیرا تصویر پروفایل null است
        verify(specialistRepository, times(1)).save(specialist);
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsException() {

        when(specialistRepository.findByEmailVerificationToken("invalid-token")).thenReturn(Optional.empty());


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> specialistService.verifyEmail("invalid-token"));
        assertEquals("Invalid verification token", exception.getMessage());
    }

    @Test
    void verifyEmail_AlreadyVerified_ThrowsException() {

        specialist.setEmailVerified(true);
        when(specialistRepository.findByEmailVerificationToken(specialist.getEmailVerificationToken()))
                .thenReturn(Optional.of(specialist));


        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> specialistService.verifyEmail(specialist.getEmailVerificationToken()));
        assertEquals("Email already verified", exception.getMessage());
    }

    // تست متد updateProfileImage
    @Test
    void updateProfileImage_ValidImage_Success() {

        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(specialistRepository.save(any(Specialist.class))).thenReturn(specialist);


        specialistService.updateProfileImage(1L, "base64Image");


        assertEquals("base64Image", specialist.getProfileImage());
        assertEquals(AccountStatus.NEW, specialist.getStatus()); // زیرا ایمیل تأیید نشده
        verify(specialistRepository, times(1)).save(specialist);
    }

    @Test
    void updateProfileImage_SpecialistNotFound_ThrowsException() {
         when(specialistRepository.findById(1L)).thenReturn(Optional.empty());

         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> specialistService.updateProfileImage(1L, "base64Image"));
        assertEquals("Specialist not found", exception.getMessage());
    }

    @Test
    void updateProfileImage_WithEmailVerified_SetsPendingStatus() {
         specialist.setEmailVerified(true);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(specialistRepository.save(any(Specialist.class))).thenReturn(specialist);

         specialistService.updateProfileImage(1L, "base64Image");

         assertEquals("base64Image", specialist.getProfileImage());
        assertEquals(AccountStatus.PENDING, specialist.getStatus());
        verify(specialistRepository, times(1)).save(specialist);
    }

    // تست متد findById
    @Test
    void findById_Exists_ReturnsSpecialist() {
         when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));


        Optional<Specialist> result = specialistService.findById(1L);


        assertTrue(result.isPresent());
        assertEquals(specialist, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(specialistRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Specialist> result = specialistService.findById(1L);

        assertFalse(result.isPresent());
    }

    // تست متد findByEmail
    @Test
    void findByEmail_Exists_ReturnsSpecialist() {

        when(specialistRepository.findByEmail("test@specialist.com")).thenReturn(Optional.of(specialist));


        Optional<Specialist> result = specialistService.findByEmail("test@specialist.com");


        assertTrue(result.isPresent());
        assertEquals(specialist, result.get());
    }

    @Test
    void findByEmail_NotExists_ReturnsEmpty() {

        when(specialistRepository.findByEmail("test@specialist.com")).thenReturn(Optional.empty());


        Optional<Specialist> result = specialistService.findByEmail("test@specialist.com");


        assertFalse(result.isPresent());
    }

    // تست متد getAll
    @Test
    void getAll_ReturnsPagedSpecialists() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialist> page = new PageImpl<>(List.of(specialist));
        when(specialistRepository.findAll(pageable)).thenReturn(page);


        Page<Specialist> result = specialistService.getAll(pageable);


        assertEquals(1, result.getTotalElements());
        assertEquals(specialist, result.getContent().get(0));
    }

    // تست متد delete
    @Test
    void delete_SpecialistExists_DeletesSuccessfully() {

        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));


        specialistService.delete(1L);


        verify(specialistRepository, times(1)).delete(specialist);
    }

    @Test
    void delete_SpecialistNotExists_NoAction() {

        when(specialistRepository.findById(1L)).thenReturn(Optional.empty());


        specialistService.delete(1L);


        verify(specialistRepository, never()).delete((Specialist) any());
    }

    // تست متد updateInfo
    @Test
    void updateInfo_NoActiveOrders_Success() {

        SpecialistUpdateDto dto = new SpecialistUpdateDto();
        dto.setPassword("newPassword");
        dto.setProfileImage("newBase64Image");
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.hasActualOrder(1L)).thenReturn(false);
        when(specialistRepository.save(any(Specialist.class))).thenReturn(specialist);


        specialistService.updateInfo(1L, dto);


        assertEquals("newPassword", specialist.getPassword());
        assertEquals("newBase64Image", specialist.getProfileImage());
        assertEquals(AccountStatus.PENDING, specialist.getStatus());
        verify(specialistRepository, times(1)).save(specialist);
    }

    @Test
    void updateInfo_HasActiveOrders_ThrowsException() {

        SpecialistUpdateDto dto = new SpecialistUpdateDto();
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.hasActualOrder(1L)).thenReturn(true);


        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> specialistService.updateInfo(1L, dto));
        assertEquals("Specialist has active work and cannot update info now.", exception.getMessage());
        verify(specialistRepository, never()).save(any());
    }

    // تست متد submitProposal
    @Test
    void submitProposal_ValidProposal_Success() {
        specialist.setStatus(AccountStatus.APPROVED);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalRepository.findBySpecialistIdAndOrderId(1L, 1L)).thenReturn(List.of());
        when(proposalRepository.save(any(Proposal.class))).thenReturn(proposal);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

         Proposal result = specialistService.submitProposal(1L, 1L, proposal);

         assertNotNull(result);
        assertEquals(ProposalStatus.PENDING, result.getStatus());
        assertEquals(OrderStatus.WAITING_FOR_SPECIALIST_SELECTION, order.getStatus());
        verify(proposalRepository, times(1)).save(proposal);
        verify(orderRepository, times(1)).save(order);
    }

    // تست متد getAvailableOrdersForSpecialist
    @Test
    void getAvailableOrdersForSpecialist_ValidSpecialist_ReturnsOrders() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order));
        specialist.setServiceCategories(List.of(serviceCategory));
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.getAvailableOrdersForSpecialist(1L, pageable)).thenReturn(page);


        Page<Order> result = specialistService.getAvailableOrdersForSpecialist(1L, pageable);


        assertEquals(1, result.getTotalElements());
        assertEquals(order, result.getContent().get(0));
    }

    @Test
    void getAvailableOrdersForSpecialist_SpecialistNotFound_ThrowsException() {

        Pageable pageable = PageRequest.of(0, 10);
        when(specialistRepository.findById(1L)).thenReturn(Optional.empty());


        assertThrows(NoSuchElementException.class,
                () -> specialistService.getAvailableOrdersForSpecialist(1L, pageable));
    }

    // تست متد getSpecialistProposals
    @Test
    void getSpecialistProposals_ReturnsProposals() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Proposal> page = new PageImpl<>(List.of(proposal));
        when(proposalRepository.findBySpecialistId(1L, pageable)).thenReturn(page);


        Page<Proposal> result = specialistService.getSpecialistProposals(1L, pageable);


        assertEquals(1, result.getTotalElements());
        assertEquals(proposal, result.getContent().get(0));
    }

    // تست متد canSubmitProposal
    @Test
    void canSubmitProposal_ValidConditions_ReturnsTrue() {

        specialist.setStatus(AccountStatus.APPROVED);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalRepository.findBySpecialistIdAndOrderId(1L, 1L)).thenReturn(List.of());


        boolean result = specialistService.canSubmitProposal(1L, 1L);


        assertTrue(result);
    }

    @Test
    void canSubmitProposal_SpecialistNotApproved_ReturnsFalse() {

        specialist.setStatus(AccountStatus.NEW);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));


        boolean result = specialistService.canSubmitProposal(1L, 1L);


        assertFalse(result);
    }

    @Test
    void canSubmitProposal_OrderNotFound_ReturnsFalse() {
        specialist.setStatus(AccountStatus.APPROVED);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = specialistService.canSubmitProposal(1L, 1L);


        assertFalse(result);
    }

    @Test
    void canSubmitProposal_AlreadySubmittedProposal_ReturnsFalse() {

        specialist.setStatus(AccountStatus.APPROVED);
        when(specialistRepository.findById(1L)).thenReturn(Optional.of(specialist));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(proposalRepository.findBySpecialistIdAndOrderId(1L, 1L)).thenReturn(List.of(proposal));


        boolean result = specialistService.canSubmitProposal(1L, 1L);


        assertFalse(result);
    }
}