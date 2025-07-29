package ir.maktab127.controller;


import ir.maktab127.dto.*;
import ir.maktab127.dto.User.UserResponseDto;
import ir.maktab127.dto.User.UserSearchFilterDto;
import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.Admin;
import ir.maktab127.service.AdminService;
import ir.maktab127.service.OrderService;
import ir.maktab127.service.ServiceCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponseDto> getById(@PathVariable Long id) {
        Optional<Admin> admin = adminService.findById(id);
        return admin.map(a -> ResponseEntity.ok(AdminMapper.toResponseDto(a)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponseDto> getAll() {
        return adminService.getAll().stream()
                .map(AdminMapper::toResponseDto)
                .collect(Collectors.toList());
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
    public List<SpecialistResponseDto> getPendingSpecialists() {
        return adminService.getPendingSpecialists().stream()
                .map(SpecialistMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }
    //سرویس
    // افزودن خدمت جدید
    @PostMapping("/service-categories")
    public ResponseEntity<ServiceCategoryResponseDto> createServiceCategory(
            @Valid @RequestBody ServiceCategoryRegisterDto dto) {
        ServiceCategory serviceCategory = ServiceCategoryMapper.toEntity(dto);
        ServiceCategory saved = serviceCategoryService.save(serviceCategory);
        return ResponseEntity.ok(ServiceCategoryMapper.toResponseDto(saved));
    }

    // ویرایش خدمت
    @PutMapping("/service-categories/{id}")
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
    @DeleteMapping("/service-categories/{id}")
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
    @GetMapping("/service-categories")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ServiceCategoryResponseDto> getAllServiceCategories() {
        return serviceCategoryService.getAll().stream()
                .map(ServiceCategoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    //اضافه و حذف متخصصان از زیرخدمتهای موجود در سیستم
    @PostMapping("/service-categories/{serviceCategoryId}/specialists/{specialistId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addSpecialistToServiceCategory(
            @PathVariable Long serviceCategoryId, @PathVariable Long specialistId) {
        adminService.addSpecialistToServiceCategory(specialistId, serviceCategoryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/service-categories/{serviceCategoryId}/specialists/{specialistId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeSpecialistFromServiceCategory(
            @PathVariable Long serviceCategoryId, @PathVariable Long specialistId) {
        adminService.removeSpecialistFromServiceCategory(specialistId, serviceCategoryId);
        return ResponseEntity.noContent().build();
    }

    //filter phase-3
    @PostMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestBody UserSearchFilterDto filter,@RequestParam(required = false) int page) {
        Page<UserResponseDto> users = adminService.searchUsers(filter, Pageable.ofSize(page));
        return   ResponseEntity.ok(users.getContent());
    }

    //
    @PostMapping("/service-history/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceHistorySummaryDto>> getServiceHistorySummary(@Valid @RequestBody ServiceHistoryFilterDto filter) {
        List<ServiceHistorySummaryDto> summary = orderService.getServiceHistorySummary(filter);
        return ResponseEntity.ok(summary);
    }

    // تاریخچه خدمات انجام شده - اطلاعات کامل یک سفارش
    @GetMapping("/service-history/detail/{orderId}")
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