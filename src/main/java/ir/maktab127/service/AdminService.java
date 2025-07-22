package ir.maktab127.service;

import java.util.List;
import java.util.Optional;

import ir.maktab127.dto.User.UserResponseDto;
import ir.maktab127.dto.User.UserSearchFilterDto;
import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Specialist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    //phase3
    Page<UserResponseDto> searchUsers(UserSearchFilterDto filter, Pageable pageable);
}