package ir.maktab127.controller;


import ir.maktab127.dto.*;
import ir.maktab127.dto.User.UserResponseDto;
import ir.maktab127.dto.User.UserSearchFilterDto;
import ir.maktab127.dto.order.OrderSummaryDTO;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.service.AdminService;
import ir.maktab127.service.OrderService;
import ir.maktab127.service.ServiceCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Validated
//
public class AdminController {
    private final AdminService adminService;
    private final ServiceCategoryService serviceCategoryService;
    private final OrderService orderService;
    @Autowired
    public AdminController(AdminService adminService, ServiceCategoryService serviceCategoryService, OrderService orderService) {
        this.adminService = adminService;
        this.serviceCategoryService = serviceCategoryService;
        this.orderService = orderService;
    }
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponseDto> register(@Valid @RequestBody AdminRegisterDto dto) {
        Admin admin = AdminMapper.toEntity(dto);
        Admin saved = adminService.save(admin);
        return ResponseEntity.ok(AdminMapper.toResponseDto(saved));
    }
    @GetMapping("/get-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponseDto> getById() {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Admin> admin = adminService.findByEmail(email);
        return admin.map(a -> ResponseEntity.ok(AdminMapper.toResponseDto(a)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/get-all-admins")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminResponseDto> getAll(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Admin> admins = adminService.getAll( pageable);
        return  admins.map(AdminMapper::toResponseDto);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/approve-specialist/{specialistId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveSpecialist(@PathVariable Long specialistId) {
        adminService.approveSpecialist(specialistId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/pending-specialists")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<SpecialistResponseDto> getPendingSpecialists(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Specialist> pendingList = adminService.getPendingSpecialists(pageable);
        return pendingList.map(SpecialistMapper::toResponseDto);


    }
    //سرویس
    // افزودن خدمت جدید
    @PostMapping("/add-service-categories")
    public ResponseEntity<ServiceCategoryResponseDto> createServiceCategory(
            @Valid @RequestBody ServiceCategoryRegisterDto dto) {
        ServiceCategory serviceCategory = ServiceCategoryMapper.toEntity(dto);
        ServiceCategory saved = serviceCategoryService.save(serviceCategory);
        return ResponseEntity.ok(ServiceCategoryMapper.toResponseDto(saved));
    }

    // ویرایش خدمت
    @PutMapping("/edit-service-categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCategoryResponseDto> updateServiceCategory(
            @PathVariable Long id,
            @Valid @RequestBody ServiceCategoryRegisterDto dto) {
        Optional<ServiceCategory> existing = serviceCategoryService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        ServiceCategory serviceCategory = ServiceCategoryMapper.toEntity(dto);
        serviceCategory.setId(id);
        ServiceCategory updated = serviceCategoryService.save(serviceCategory);
        return ResponseEntity.ok(ServiceCategoryMapper.toResponseDto(updated));
    }

    // حذف خدمت
    @DeleteMapping("/delete-service-categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteServiceCategory(@PathVariable Long id) {
        serviceCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // مشاهده یک خدمت
    @GetMapping("/service-categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCategoryResponseDto> getServiceCategory(@PathVariable Long id) {
        return serviceCategoryService.findById(id)
                .map(ServiceCategoryMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // مشاهده همه خدمات
    @GetMapping("/all-service-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ServiceCategoryResponseDto> getAllServiceCategories(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceCategory> services= serviceCategoryService.getAll(pageable);
        return  services.map(ServiceCategoryMapper::toResponseDto);
    }
    //اضافه و حذف متخصصان از زیرخدمتهای موجود در سیستم
    @PostMapping("/service-categories/{serviceCategoryId}/specialists/{specialistId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addSpecialistToServiceCategory(
            @PathVariable Long serviceCategoryId, @PathVariable Long specialistId) {
        try {
            adminService.addSpecialistToServiceCategory(specialistId, serviceCategoryId);
            return ResponseEntity.ok(new ApiResponseDto("Specialist successfully added to service category", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto("An unexpected error occurred: " + e.getMessage(), false));
        }
    }


    @DeleteMapping("/service-categories/{serviceCategoryId}/specialists/{specialistId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> removeSpecialistFromServiceCategory(
            @PathVariable Long serviceCategoryId, @PathVariable Long specialistId) {
        adminService.removeSpecialistFromServiceCategory(specialistId, serviceCategoryId);
        return ResponseEntity.ok(new ApiResponseDto("Specialist successfully removed his service category", true));
    }

    //filter phase-3
    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(@RequestBody UserSearchFilterDto filter
            ,@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int size,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        Page<UserResponseDto> users = adminService.searchUsers(filter, pageable);
        return   ResponseEntity.ok(users);
    }


    @GetMapping("/service-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderSummaryDTO>> getServiceHistorySummary(@Valid @RequestBody ServiceHistoryFilterDto filter,@RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "1") int size,
    @RequestParam(required = false) String sort){
        Pageable pageable = PageRequest.of(page-1, size);
        Page<OrderSummaryDTO> summary = orderService.getServiceHistorySummary(filter,pageable);
        return ResponseEntity.ok(summary);
    }

    // تاریخچه خدمات انجام شده - اطلاعات کامل یک سفارش
    @GetMapping("/service-history/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceHistoryDetailDto> getServiceHistoryDetail(@PathVariable Long orderId) {
        ServiceHistoryDetailDto detail = orderService.getServiceHistoryDetail(orderId);
        return ResponseEntity.ok(detail);
    }

}
//HOTFIX-PH3
//UNIT TEST
//        PERFORMANCE QUERY
// VALIDATION CHARGE WalletController
//ORDER 1
//