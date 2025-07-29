package ir.maktab127.security;


import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.AdminRepository;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.SpecialistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final AdminRepository adminRepository;

    private final SpecialistRepository specialistRepository;


    private final CustomerRepository customerRepository;


    public CustomUserDetailsService(AdminRepository adminRepository, SpecialistRepository specialistRepository, CustomerRepository customerRepository) {
        this.adminRepository = adminRepository;
        this.specialistRepository = specialistRepository;
        this.customerRepository = customerRepository;

    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check in Admin table
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            return new CustomUserDetails(adminOpt.get());
        }

        Optional<Specialist> specialistOpt = specialistRepository.findByEmail(email);
        if (specialistOpt.isPresent()) {
            return new CustomUserDetails(specialistOpt.get());
        }


        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isPresent()) {
            return new CustomUserDetails(customerOpt.get());
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}