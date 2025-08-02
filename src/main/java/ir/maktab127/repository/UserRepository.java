package ir.maktab127.repository;

import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository<T extends User> extends JpaRepository<User, Long> {

    Optional<T > findByEmail(String email);
}
