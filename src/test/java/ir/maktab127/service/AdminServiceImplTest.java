package ir.maktab127.service;

import ir.maktab127.dto.User.UserResponseDto;
import ir.maktab127.dto.User.UserSearchFilterDto;
import ir.maktab127.entity.Comment;
import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.AdminRepository;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.ServiceCategoryRepository;
import ir.maktab127.repository.SpecialistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin admin;
    private Specialist specialist;
    private Customer customer;
    private ServiceCategory serviceCategory;
    private UserSearchFilterDto filterDto;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setId(1L);
        admin.setEmail("admin@test.com");

        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);
        serviceCategory.setName("Test Service");

        specialist = new Specialist();
        specialist.setId(2L);
        specialist.setFirstName("Jane");
        specialist.setLastName("Smith");
        specialist.setEmail("specialist@test.com");
        specialist.setStatus(AccountStatus.PENDING);
        specialist.setServiceCategories(new ArrayList<>(Arrays.asList(serviceCategory)));

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setRating(5);
        comment.setSpecialist(specialist);
        specialist.setComments(Arrays.asList(comment));

        customer = new Customer();
        customer.setId(3L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("customer@test.com");

        filterDto = new UserSearchFilterDto();
        filterDto.setFirstName("Jane");
        filterDto.setLastName("Smith");
        filterDto.setServiceName("Test Service");
        filterDto.setMinScore(0);
        filterDto.setMaxScore(10);
        filterDto.setRole("SPECIALIST");
    }

    @Test
    void save_ValidAdmin_ReturnsSavedAdmin() {
        when(adminRepository.save(admin)).thenReturn(admin);

        Admin result = adminService.save(admin);

        assertNotNull(result);
        assertEquals(admin, result);
        verify(adminRepository, times(1)).save(admin);
    }

    @Test
    void findById_Exists_ReturnsAdmin() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        Optional<Admin> result = adminService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(admin, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Admin> result = adminService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_Exists_ReturnsAdmin() {
        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        Optional<Admin> result = adminService.findByEmail("admin@test.com");

        assertTrue(result.isPresent());
        assertEquals(admin, result.get());
    }

    @Test
    void findByEmail_NotExists_ReturnsEmpty() {
        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());

        Optional<Admin> result = adminService.findByEmail("admin@test.com");

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsPagedAdmins() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Admin> page = new PageImpl<>(Arrays.asList(admin));
        when(adminRepository.findAll(pageable)).thenReturn(page);

        Page<Admin> result = adminService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(admin, result.getContent().get(0));
    }

    @Test
    void delete_AdminExists_DeletesSuccessfully() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        adminService.delete(1L);

        verify(adminRepository, times(1)).delete(admin);
    }

    @Test
    void delete_AdminNotExists_NoAction() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());

        adminService.delete(1L);

        verify(adminRepository, never()).delete(any());
    }

    @Test
    void approveSpecialist_SpecialistExists_SetsApprovedStatus() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(specialistRepository.save(specialist)).thenReturn(specialist);

        adminService.approveSpecialist(2L);

        assertEquals(AccountStatus.APPROVED, specialist.getStatus());
        verify(specialistRepository, times(1)).save(specialist);
    }

    @Test
    void approveSpecialist_SpecialistNotExists_NoAction() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.empty());

        adminService.approveSpecialist(2L);

        verify(specialistRepository, never()).save(any());
    }

    @Test
    void getPendingSpecialists_ReturnsPagedSpecialists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialist> page = new PageImpl<>(Arrays.asList(specialist));
        when(specialistRepository.findByStatus(AccountStatus.PENDING, pageable)).thenReturn(page);

        Page<Specialist> result = adminService.getPendingSpecialists(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(specialist, result.getContent().get(0));
    }

    @Test
    void addSpecialistToServiceCategory_ValidData_Success() {
        ServiceCategory newServiceCategory = new ServiceCategory();
        newServiceCategory.setId(2L);
        newServiceCategory.setName("New Service");
        specialist.setServiceCategories(new ArrayList<>());
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(newServiceCategory));
        when(specialistRepository.save(specialist)).thenReturn(specialist);

        adminService.addSpecialistToServiceCategory(2L, 2L);

        assertTrue(specialist.getServiceCategories().contains(newServiceCategory));
        verify(specialistRepository, times(1)).save(specialist);
    }

    @Test
    void addSpecialistToServiceCategory_SpecialistNotFound_ThrowsException() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.addSpecialistToServiceCategory(2L, 1L));
    }

    @Test
    void addSpecialistToServiceCategory_ServiceNotFound_ThrowsException() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.addSpecialistToServiceCategory(2L, 1L));
    }

    @Test
    void addSpecialistToServiceCategory_AlreadyContainsServiceCategory_NoAction() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(serviceCategory));

        adminService.addSpecialistToServiceCategory(2L, 1L);

        verify(specialistRepository, never()).save(any());
    }

    @Test
    void removeSpecialistFromServiceCategory_ValidData_Success() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(serviceCategory));
        when(specialistRepository.save(specialist)).thenReturn(specialist);

        adminService.removeSpecialistFromServiceCategory(2L, 1L);

        assertFalse(specialist.getServiceCategories().contains(serviceCategory));
        verify(specialistRepository, times(1)).save(specialist);
    }

    @Test
    void removeSpecialistFromServiceCategory_SpecialistNotFound_ThrowsException() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.removeSpecialistFromServiceCategory(2L, 1L));
    }

    @Test
    void removeSpecialistFromServiceCategory_ServiceNotFound_ThrowsException() {
        when(specialistRepository.findById(2L)).thenReturn(Optional.of(specialist));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.removeSpecialistFromServiceCategory(2L, 1L));
    }


    }