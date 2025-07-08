package ir.maktab127.service;

import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.AdminRepository;
import ir.maktab127.repository.ServiceCategoryRepository;
import ir.maktab127.repository.SpecialistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final SpecialistRepository  specialistRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    public AdminServiceImpl(AdminRepository adminRepository, SpecialistRepository specialistRepository, ServiceCategoryRepository serviceCategoryRepository ) {
        this.adminRepository = adminRepository;
        this.specialistRepository = specialistRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;

    }
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

}