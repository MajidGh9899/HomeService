package ir.maktab127.controller;


import ir.maktab127.dto.*;
import ir.maktab127.dto.User.UserResponseDto;
import ir.maktab127.dto.User.UserSearchFilterDto;
import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.Admin;
import ir.maktab127.service.AdminService;
import ir.maktab127.service.ServiceCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    public AdminController(AdminService adminService, ServiceCategoryService serviceCategoryService) {
        this.adminService = adminService;
        this.serviceCategoryService = serviceCategoryService;
    }
    @PostMapping("/register")
    public ResponseEntity<AdminResponseDto> register(@Valid @RequestBody AdminRegisterDto dto) {
        Admin admin = AdminMapper.toEntity(dto);
        Admin saved = adminService.save(admin);
        return ResponseEntity.ok(AdminMapper.toResponseDto(saved));
    }
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDto> getById(@PathVariable Long id) {
        Optional<Admin> admin = adminService.findById(id);
        return admin.map(a -> ResponseEntity.ok(AdminMapper.toResponseDto(a)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping
    public List<AdminResponseDto> getAll() {
        return adminService.getAll().stream()
                .map(AdminMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/approve-specialist/{specialistId}")
    public ResponseEntity<Void> approveSpecialist(@PathVariable Long specialistId) {
        adminService.approveSpecialist(specialistId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/pending-specialists")
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
    public ResponseEntity<Void> deleteServiceCategory(@PathVariable Long id) {
        serviceCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // مشاهده یک خدمت
    @GetMapping("/service-categories/{id}")
    public ResponseEntity<ServiceCategoryResponseDto> getServiceCategory(@PathVariable Long id) {
        return serviceCategoryService.findById(id)
                .map(ServiceCategoryMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // مشاهده همه خدمات
    @GetMapping("/service-categories")
    public List<ServiceCategoryResponseDto> getAllServiceCategories() {
        return serviceCategoryService.getAll().stream()
                .map(ServiceCategoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    //اضافه و حذف متخصصان از زیرخدمتهای موجود در سیستم
    @PostMapping("/service-categories/{serviceCategoryId}/specialists/{specialistId}")
    public ResponseEntity<Void> addSpecialistToServiceCategory(
            @PathVariable Long serviceCategoryId, @PathVariable Long specialistId) {
        adminService.addSpecialistToServiceCategory(specialistId, serviceCategoryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/service-categories/{serviceCategoryId}/specialists/{specialistId}")
    public ResponseEntity<Void> removeSpecialistFromServiceCategory(
            @PathVariable Long serviceCategoryId, @PathVariable Long specialistId) {
        adminService.removeSpecialistFromServiceCategory(specialistId, serviceCategoryId);
        return ResponseEntity.noContent().build();
    }

    //filter phase-3
    @PostMapping("/users/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestBody UserSearchFilterDto filter) {
        List<UserResponseDto> users = adminService.searchUsers(filter);
        return ResponseEntity.ok(users);
    }

}
