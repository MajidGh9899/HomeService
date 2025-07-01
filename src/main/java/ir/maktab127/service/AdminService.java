package ir.maktab127.service;

import java.util.List;
import java.util.Optional;

import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Specialist;

public interface AdminService {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    Optional<Admin> findByEmail(String email);
    List<Admin> getAll();
    void delete(Long id);


    void approveSpecialist(Long specialistId);
    List<Specialist> getPendingSpecialists();
    void addSpecialistToServiceCategory(Long specialistId, Long serviceCategoryId);
    void removeSpecialistFromServiceCategory(Long specialistId, Long serviceCategoryId);
}