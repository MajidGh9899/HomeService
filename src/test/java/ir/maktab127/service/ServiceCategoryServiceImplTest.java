package ir.maktab127.service;

import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.repository.ServiceCategoryRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceCategoryServiceImplTest {

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @InjectMocks
    private ServiceCategoryServiceImpl serviceCategoryService;

    private ServiceCategory serviceCategory;

    @BeforeEach
    void setUp() {
        serviceCategory = new ServiceCategory();
        serviceCategory.setId(1L);
        serviceCategory.setName("Test Service");
        serviceCategory.setParent(null);
    }

    @Test
    void save_ValidServiceCategory_ReturnsSavedServiceCategory() {
        when(serviceCategoryRepository.save(serviceCategory)).thenReturn(serviceCategory);

        ServiceCategory result = serviceCategoryService.save(serviceCategory);

        assertNotNull(result);
        assertEquals(serviceCategory, result);
        verify(serviceCategoryRepository, times(1)).save(serviceCategory);
    }

    @Test
    void findById_Exists_ReturnsServiceCategory() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(serviceCategory));

        Optional<ServiceCategory> result = serviceCategoryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(serviceCategory, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ServiceCategory> result = serviceCategoryService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByNameAndParentId_Exists_ReturnsServiceCategory() {
        when(serviceCategoryRepository.findByNameAndParentId("Test Service", null))
                .thenReturn(Optional.of(serviceCategory));

        Optional<ServiceCategory> result = serviceCategoryService.findByNameAndParentId("Test Service", null);

        assertTrue(result.isPresent());
        assertEquals(serviceCategory, result.get());
    }

    @Test
    void findByNameAndParentId_NotExists_ReturnsEmpty() {
        when(serviceCategoryRepository.findByNameAndParentId("Test Service", null))
                .thenReturn(Optional.empty());

        Optional<ServiceCategory> result = serviceCategoryService.findByNameAndParentId("Test Service", null);

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsPagedServiceCategories() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceCategory> page = new PageImpl<>(Arrays.asList(serviceCategory));
        when(serviceCategoryRepository.findAll(pageable)).thenReturn(page);

        Page<ServiceCategory> result = serviceCategoryService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(serviceCategory, result.getContent().get(0));
    }

    @Test
    void delete_ServiceCategoryExists_DeletesSuccessfully() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(serviceCategory));

        serviceCategoryService.delete(1L);

        verify(serviceCategoryRepository, times(1)).delete(serviceCategory);
    }

    @Test
    void delete_ServiceCategoryNotExists_NoAction() {
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        serviceCategoryService.delete(1L);

        verify(serviceCategoryRepository, never()).delete(any());
    }
}