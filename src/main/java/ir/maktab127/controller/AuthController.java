package ir.maktab127.controller;


import ir.maktab127.dto.*;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.CustomerRepository;
import ir.maktab127.security.CustomUserDetails;
import ir.maktab127.security.JwtTokenUtil;
import ir.maktab127.service.AdminService;
import ir.maktab127.service.CustomerService;
import ir.maktab127.service.SpecialistService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")

public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;



    @Autowired
    private SpecialistService   specialistService;

    @Autowired
    private CustomerService  customerService;
    @Autowired
    private CustomerRepository customerRepository;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails.getUsername());

            LoginResponseDto response = new LoginResponseDto();
            response.setToken(token);
            response.setEmail(userDetails.getUsername());
            response.setRoles(userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toSet()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password !!!");
        }

    }
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {

            try {
                specialistService.verifyEmail(token);
                return ResponseEntity.ok(Map.of("message", "Specialist email verified successfully"));
            } catch (IllegalArgumentException e) {
                // If not a specialist, try customer
                customerService.verifyEmail(token);
                return ResponseEntity.ok(Map.of("message", "Customer email verified successfully"));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/customer/register")
    public ResponseEntity<CustomerResponseDto> register(@Valid @RequestBody CustomerRegisterDto dto) throws MessagingException {
        Customer customer = CustomerMapper.toEntity(dto);
        Customer saved = customerService.register(customer);
        return ResponseEntity.ok(CustomerMapper.toResponseDto(saved));
    }

    @PostMapping(value = "/specialist/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @Valid @RequestPart("dto") SpecialistRegisterDto dto,
            @RequestPart(value = "profileImageUp", required = false) MultipartFile profileImageUp) throws IOException, MessagingException {

        // اعتبارسنجی اندازه و فرمت فایل
        if (profileImageUp != null && !profileImageUp.isEmpty()) {
            long maxSizeInBytes = 300 * 1024; // 300 KB


            if (profileImageUp.getSize() > maxSizeInBytes) {
                return ResponseEntity.badRequest().body("Max image size is 300 KB");
            }
            String base64Image = Base64.getEncoder().encodeToString(profileImageUp.getBytes());
            dto.setProfileImage(base64Image);
        }

        Specialist specialist = SpecialistMapper.toEntity(dto);
        Specialist saved = specialistService.register(specialist);
        return ResponseEntity.ok(SpecialistMapper.toResponseDto(saved));
    }


//    @PostMapping("/register/customer")
//    public ResponseEntity<?> registerCustomer(@RequestBody RegisterRequest registerRequest) {
//        if (adminRepository.findByEmail(registerRequest.getEmail()).isPresent() ||
//                specialistRepository.findByEmail(registerRequest.getEmail()).isPresent() ||
//                customerRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
//            return ResponseEntity.badRequest().body("Email already exists");
//        }
//


//
//    @PostMapping("/register/specialist")
//    public ResponseEntity<?> registerSpecialist(@RequestBody RegisterRequest registerRequest) {
//        if (adminRepository.findByEmail(registerRequest.getEmail()).isPresent() ||
//                specialistRepository.findByEmail(registerRequest.getEmail()).isPresent() ||
//                customerRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
//            return ResponseEntity.badRequest().body("Email already exists");
//        }
//
//        Specialist specialist = new Specialist();
//        specialist.setFirstName(registerRequest.getFirstName());
//        specialist.setLastName(registerRequest.getLastName());
//        specialist.setEmail(registerRequest.getEmail());
//        specialist.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
//        specialist.setRegisterDate(LocalDateTime.now());
//
//        Set<Role> roles = new HashSet<>();
//        roles.add(Role.EXPERT);
//        specialist.setRoles(roles);
//
//        specialistRepository.save(specialist);
//
//        return ResponseEntity.ok("Specialist registered successfully");
//    }
}