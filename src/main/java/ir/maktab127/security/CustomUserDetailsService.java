package ir.maktab127.security;


import ir.maktab127.entity.user.Admin;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.entity.user.User;
import ir.maktab127.repository.AdminRepository;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.repository.SpecialistRepository;
import ir.maktab127.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final AdminRepository adminRepository;

    private final SpecialistRepository specialistRepository;


    private final CustomerRepository customerRepository;


    private final UserRepository<User> userRepository;





    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check in Admin table
        Optional< User> userOpt=userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
//        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
//        if (adminOpt.isPresent()) {
//            return adminOpt.get();
//        }
//
//        Optional<Specialist> specialistOpt = specialistRepository.findByEmail(email);
//        if (specialistOpt.isPresent()) {
//            return specialistOpt.get();
//        }
//
//
//        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
//        if (customerOpt.isPresent()) {
//            return customerOpt.get();
//        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}