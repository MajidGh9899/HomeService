package ir.maktab127.repository;


import ir.maktab127.entity.user.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface AdminRepository extends JpaRepository<Admin, Long> {


    Optional<Admin> findByEmail(String email);


}
