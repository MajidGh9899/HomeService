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
import ir.maktab127.repository.specification.CustomerSpecification;
import ir.maktab127.repository.specification.SpecialistSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final SpecialistRepository  specialistRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    @Override
    public Admin save(Admin admin) { return adminRepository.save(admin); }
    @Override
    public Optional<Admin> findById(Long id) { return adminRepository.findById(id); }
    @Override
    public Optional<Admin> findByEmail(String email) { return adminRepository.findByEmail(email); }
    @Override
    public Page<Admin> getAll(Pageable page) { return  adminRepository.findAll(page); }

    @Transactional
    @Override

    public void delete(Long id) { adminRepository.findById(id).ifPresent(adminRepository::delete); }
    @Transactional
    @Override
    public void approveSpecialist(Long specialistId) {
        specialistRepository.findById(specialistId).ifPresent(specialist -> {
            specialist.setStatus(AccountStatus.APPROVED);
            specialistRepository.save(specialist);
        });

    }
    @Override
    public Page<Specialist> getPendingSpecialists(  Pageable page) {
        return  specialistRepository.findByStatus(AccountStatus.PENDING, page);

    }
    @Override
    public void addSpecialistToServiceCategory(Long specialistId, Long serviceCategoryId) {
        Specialist specialist = specialistRepository.findById(specialistId).orElseThrow();
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId).orElseThrow();
        if (!specialist.getServiceCategories().contains(serviceCategory)) {
            specialist.getServiceCategories().add(serviceCategory);
            specialistRepository.save(specialist);
        }
    }
    @Transactional
    @Override
    public void removeSpecialistFromServiceCategory(Long specialistId, Long serviceCategoryId) {
        Specialist specialist = specialistRepository.findById(specialistId).orElseThrow();
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId).orElseThrow();
        specialist.getServiceCategories().remove(serviceCategory);
        specialistRepository.save(specialist);
    }
    @Transactional
    @Override
    public Page<UserResponseDto> searchUsers(UserSearchFilterDto filter, Pageable pageable) {
        List<UserResponseDto> result = new ArrayList<>();

        // جست‌وجوی متخصص‌ها
        if (filter.getRole() == null || filter.getRole().equalsIgnoreCase("SPECIALIST")) {
            specialistSearch(filter, pageable, result);
        }

        // جست‌وجوی مشتریان
        if (filter.getRole() == null || filter.getRole().equalsIgnoreCase("CUSTOMER")) {
            customerSearch(filter, pageable, result);
        }




        return  new PageImpl<>(result, pageable, result.size());
    }

    private void specialistSearch(UserSearchFilterDto filter, Pageable pageable, List<UserResponseDto> result) {
        Page<Specialist> specialists = specialistRepository.findAll(
                SpecialistSpecification.searchWithFilters(
                        filter.getFirstName(),
                        filter.getLastName(),
                        filter.getServiceName(),
                        filter.getMinScore(),
                        filter.getMaxScore()
                ),
                pageable
        );

        result.addAll(specialists.getContent().stream().map(s -> {
            UserResponseDto dto = new UserResponseDto();
            dto.setId(s.getId());
            dto.setRole("SPECIALIST");
            dto.setFirstName(s.getFirstName());
            dto.setLastName(s.getLastName());
            dto.setEmail(s.getEmail());
            // محاسبه میانگین امتیاز
            dto.setScore(s.getComments().stream().mapToInt(Comment::getRating).average().orElse(0));
            if (s.getServiceCategories() != null && !s.getServiceCategories().isEmpty()) {
                dto.setServiceName(s.getServiceCategories().stream()
                        .map(ServiceCategory::getName)
                        .collect(Collectors.joining(", ")));
            }
            return dto;
        }).toList());
    }

    private void customerSearch(UserSearchFilterDto filter, Pageable pageable, List<UserResponseDto> result) {
        Page<Customer> customers = customerRepository.findAll(
                CustomerSpecification.searchWithFilters(
                        filter.getFirstName(),
                        filter.getLastName()
                ),
                pageable
        );

        result.addAll(customers.getContent().stream().map(c -> {
            UserResponseDto dto = new UserResponseDto();
            dto.setId(c.getId());
            dto.setRole("CUSTOMER");
            dto.setFirstName(c.getFirstName());
            dto.setLastName(c.getLastName());
            dto.setEmail(c.getEmail());
            return dto;
        }).toList());
    }

}