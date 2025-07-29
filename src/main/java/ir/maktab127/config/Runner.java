package ir.maktab127.config;


import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Role;
import ir.maktab127.repository.AdminRepository;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.SpecialistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SpecialistRepository specialistRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default admin if none exists
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setFirstName("admin");
            admin.setLastName("system");

            admin.setEmail("admin@system.com");
            admin.setPassword(passwordEncoder.encode("admin123"));


            Set<Role> roles = new HashSet<>();
            roles.add(Role.ADMIN);
            admin.setRoles(roles);

            adminRepository.save(admin);
            System.out.println("Default admin created: admin@system.com / admin123");
        }

        // Set default roles for existing users without roles
//        adminRepository.findAll().forEach(admin -> {
//            if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
//                Set<Role> roles = new HashSet<>();
//                roles.add(Role.ADMIN);
//                admin.setRoles(roles);
//                adminRepository.save(admin);
//            }
//        });
//
//        specialistRepository.findAll().forEach(specialist -> {
//            if (specialist.getRoles() == null || specialist.getRoles().isEmpty()) {
//                Set<Role> roles = new HashSet<>();
//                roles.add(Role.SPECIALIST);
//                specialist.setRoles(roles);
//                specialistRepository.save(specialist);
//            }
//        });
//
//        customerRepository.findAll().forEach(customer -> {
//            if (customer.getRoles() == null || customer.getRoles().isEmpty()) {
//                Set<Role> roles = new HashSet<>();
//                roles.add(Role.CUSTOMER);
//                customer.setRoles(roles);
//                customerRepository.save(customer);
//            }
//        });
    }
}