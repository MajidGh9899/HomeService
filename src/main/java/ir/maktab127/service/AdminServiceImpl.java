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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Override
    public Admin save(Admin admin) { return adminRepository.save(admin); }
    @Override
    public Optional<Admin> findById(Long id) { return adminRepository.findById(id); }
    @Override
    public Optional<Admin> findByEmail(String email) { return adminRepository.findByEmail(email); }
    @Override
    public List<Admin> getAll() { return adminRepository.findAll(); }
    @Override
    public void delete(Long id) { adminRepository.findById(id).ifPresent(adminRepository::delete); }

    @Override
    public void approveSpecialist(Long specialistId) {
        specialistRepository.findById(specialistId).ifPresent(specialist -> {
            specialist.setStatus(AccountStatus.APPROVED);
            specialistRepository.save(specialist);
        });

    }
    @Override
    public List<Specialist> getPendingSpecialists() {
        return specialistRepository.findAll().stream()
                .filter(s -> s.getStatus() == AccountStatus.NEW || s.getStatus() == AccountStatus.PENDING)
                .toList();
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

    @Override
    public void removeSpecialistFromServiceCategory(Long specialistId, Long serviceCategoryId) {
        Specialist specialist = specialistRepository.findById(specialistId).orElseThrow();
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(serviceCategoryId).orElseThrow();
        specialist.getServiceCategories().remove(serviceCategory);
        specialistRepository.save(specialist);
    }

    @Override
    public List<UserResponseDto> searchUsers(UserSearchFilterDto filter) {
        List<UserResponseDto> result = new ArrayList<>();

        if (filter.getRole() == null || filter.getRole().equalsIgnoreCase("SPECIALIST")) {
            List<Specialist> specialists = specialistRepository.searchWithFilters(
                    filter.getFirstName(),
                    filter.getLastName(),
                    filter.getServiceName(),
                    filter.getMinScore(),
                    filter.getMaxScore()
            );

            // اعمال فیلترهای minScore و maxScore در جاوا
            if (filter.getMinScore() != null || filter.getMaxScore() != null) {
                specialists = specialists.stream()
                        .filter(s -> {
                            double avgRating = s.getComments().stream()
                                    .mapToDouble(Comment::getRating).average().orElse(0.0);

                            boolean meetsMin = filter.getMinScore() == null || avgRating >= filter.getMinScore();
                            boolean meetsMax = filter.getMaxScore() == null || avgRating <= filter.getMaxScore();

                            return meetsMin && meetsMax;
                        })
                        .toList();
            }

            result.addAll(specialists.stream().map(s -> {
                UserResponseDto dto = new UserResponseDto();
                dto.setId(s.getId());
                dto.setRole("SPECIALIST");
                dto.setFirstName(s.getFirstName());
                dto.setLastName(s.getLastName());
                dto.setEmail(s.getEmail());

                double avgRating = s.getComments().stream()
                        .mapToDouble(Comment::getRating).average().orElse(0.0);
                dto.setScore(avgRating);

                if (s.getServiceCategories() != null && !s.getServiceCategories().isEmpty()) {
                    dto.setServiceName(s.getServiceCategories().stream()
                            .map(ServiceCategory::getName)
                            .collect(Collectors.joining(", ")));
                }

                return dto;
            }).toList());
        }

        if (filter.getRole() == null || filter.getRole().equalsIgnoreCase("CUSTOMER")) {
            List<Customer> customers = customerRepository.searchWithFilters(
                    filter.getFirstName(),
                    filter.getLastName()
            );

            result.addAll(customers.stream().map(c -> {
                UserResponseDto dto = new UserResponseDto();
                dto.setId(c.getId());
                dto.setRole("CUSTOMER");
                dto.setFirstName(c.getFirstName());
                dto.setLastName(c.getLastName());
                dto.setEmail(c.getEmail());
                return dto;
            }).toList());
        }

        return result;
    }

}