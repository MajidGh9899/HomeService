package ir.maktab127.repository;


import ir.maktab127.entity.user.Admin;

import java.util.List;
import java.util.Optional;
public interface AdminRepository {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    Optional<Admin> findByEmail(String email);
    List<Admin> findAll();
    void delete(Admin admin);

}
